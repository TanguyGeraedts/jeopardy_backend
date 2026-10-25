# Jeopardy on the VPS

Postgres, the backend and the frontend run with Docker Compose in `/opt/jeopardy`, next to the lobby stack in `/opt/lobby`.
nginx on the host serves `jeopardy.geraedts.dev` from the frontend on `127.0.0.1:3200`. Browsers never reach the backend
directly: the frontend forwards `/api` to it ([ADR 0001](../../docs/adr/0001-backend-only-reachable-through-the-frontend.md)).

```
browser ── https ──> nginx ──> jeopardy-frontend :3200 ──/api──> jeopardy-backend ──> postgres
                                                                   └── gamenet ──> lobby-backend
```

A push to `main` in either Jeopardy repo tests, builds `ghcr.io/tanguygeraedts/jeopardy_backend` or `…/jeopardy_frontend`,
and runs `deploy-jeopardy` on the VPS, which pulls both images and restarts what changed.

## One-time setup

Do these in order. Steps 1-4 happen outside the VPS.

### 1. DNS

Add an `A` record (and `AAAA` if the VPS has IPv6) for `jeopardy.geraedts.dev` pointing at the VPS. Wait until
`nslookup jeopardy.geraedts.dev` returns the VPS address before step 6 (the certificate needs it).

### 2. Clerk production instance

In the Clerk dashboard ([ADR 0002](../../docs/adr/0002-clerk-production-instance-owns-quizzes.md)):

1. Create a **production** instance of the Jeopardy application, domain `geraedts.dev`.
2. Add the DNS records Clerk lists (e.g. `clerk.geraedts.dev`, `accounts.geraedts.dev` and the email records) and wait
   until Clerk shows them as verified.
3. **Google and Discord:** production needs your own OAuth apps (the development instance used Clerk's shared ones).
   In Clerk, open each social connection, turn on "Use custom credentials", and copy the redirect URI it shows into a
   new OAuth app in the [Google Cloud console](https://console.cloud.google.com/apis/credentials) and the
   [Discord developer portal](https://discord.com/developers/applications). Paste their client ID and secret back into Clerk.
4. Copy from Configure > API keys: the **Frontend API URL** (→ `CLERK_ISSUER`), the **publishable key** `pk_live_…`
   (→ GitHub variable, step 3) and the **secret key** `sk_live_…` (→ `.env`, step 5).

### 3. GitHub

**jeopardy_frontend** repo, Settings > Secrets and variables > Actions > **Variables**:
- `NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY` = the `pk_live_…` key

**Both** Jeopardy repos, **Secrets** (same values as the lobby repos, except the key):
- `VPS_HOST`, `VPS_USER` (`deploy`), `VPS_PORT` (if not 22), `VPS_KNOWN_HOSTS`
- `VPS_SSH_KEY` = the private key `jeopardy_deploy_key` from step 4 (not the lobby or nginx key)

### 4. Deploy key and script (VPS, as root)

On your PC: `ssh-keygen -t ed25519 -f jeopardy_deploy_key -N "" -C "github-actions-jeopardy"`, then copy the files:

```bash
scp jeopardy_deploy_key.pub ../vps-infrastructure/scripts/deploy-jeopardy.sh root@your-vps:/tmp/
```

On the VPS:

```bash
install -o root -g root -m 755 /tmp/deploy-jeopardy.sh /usr/local/bin/deploy-jeopardy && rm /tmp/deploy-jeopardy.sh
echo 'deploy ALL=(root) NOPASSWD: /usr/local/bin/deploy-jeopardy ""' > /etc/sudoers.d/deploy-jeopardy
chmod 440 /etc/sudoers.d/deploy-jeopardy && visudo -c
echo "restrict,command=\"sudo -n /usr/local/bin/deploy-jeopardy\" $(cat /tmp/jeopardy_deploy_key.pub)" >> /home/deploy/.ssh/authorized_keys
rm /tmp/jeopardy_deploy_key.pub
```

The images are private on GHCR by default. The VPS pulls with the `ghcr.io` login root already has for the lobby;
that token needs `read:packages` (a classic token with that scope reads all your packages). If a pull says
"denied", run `docker login ghcr.io` as root again with such a token.

### 5. The stack (VPS, as root)

```bash
mkdir -p /opt/jeopardy && cd /opt/jeopardy
# from your PC: scp deploy/vps/docker-compose.yml deploy/vps/.env.example deploy/vps/backup.sh root@your-vps:/opt/jeopardy/
cp .env.example .env && chmod 600 .env && nano .env      # POSTGRES_PASSWORD, CLERK_ISSUER, CLERK_SECRET_KEY
chmod 700 backup.sh
ls -l /opt/lobby/secrets/public.pem                      # the lobby's production public key must exist
```

Don't start it yet: the images only exist after step 7.

### 6. nginx

Push `vps-infrastructure` (it contains `nginx/sites/jeopardy.geraedts.dev.conf`). Its workflow installs the site and
gets the certificate; DNS from step 1 must already point here.

### 7. First deploy

Push `main` of **both** Jeopardy repos. Each workflow builds its image and then runs `deploy-jeopardy`; the first one to
finish fails its deploy step because the other image doesn't exist yet. Once both images are built, re-run that failed
deploy job (or run `sudo deploy-jeopardy` on the VPS). Flyway creates the tables on the first start.

### 8. Backups

```bash
crontab -e     # as root, add:
30 3 * * * /opt/jeopardy/backup.sh >> /var/log/jeopardy-backup.log 2>&1
```

Dumps go to `/opt/jeopardy/backups`, 7 days are kept. They stay on the VPS only: copy one elsewhere now and then.

## Check it works

- `cd /opt/jeopardy && docker compose ps`: three containers, `postgres` healthy.
- `docker compose logs jeopardy-backend | grep -i "started\|flyway"`: migrations applied, app started.
- `https://jeopardy.geraedts.dev`: sign in with Google or Discord (no "Development mode" badge).
- Create a quiz, publish it, find it in Discover.
- Host it, join the lobby from a phone, start: the phone opens `jeopardy.geraedts.dev/play/…`.
- Buzz: the board and admin page update instantly (if they only change after a refresh, the live stream is buffered:
  check the nginx `/events` location).

## Updating

Push to `main`. To restart by hand: `sudo deploy-jeopardy`. To change settings: edit `.env`, then
`cd /opt/jeopardy && docker compose up -d`. If you change `docker-compose.yml`, copy it to the VPS again first.
