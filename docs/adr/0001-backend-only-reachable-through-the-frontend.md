# The backend is only reachable through the frontend

Browsers never talk to the Spring backend directly: they call `/api/*` on the Jeopardy site (`jeopardy.geraedts.dev`), and Next.js forwards it to the backend over the VPS's private Docker network. We chose this over a public `api.` subdomain so there is one origin (no CORS setup, one certificate, Clerk tokens and the live SSE stream stay same-origin) and the backend has no internet-facing port.

## Consequences

- `BACKEND_URL` is baked into the frontend image at build time (Next rewrites are resolved then), so moving the backend means rebuilding the frontend.
- The live game stream (`/api/v1/game-sessions/{id}/events`) passes through nginx and Next; both must not buffer or compress it. The backend sends `Cache-Control: no-transform` and `X-Accel-Buffering: no` for this, and nginx has an unbuffered location for it.
