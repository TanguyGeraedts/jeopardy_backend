# Host logins use a Clerk production instance, and a quiz's owner is a Clerk user id

Hosts sign in with Clerk; the backend stores each quiz's owner as the Clerk user id from the token. Production uses its own Clerk production instance on `geraedts.dev` with our own Google and Discord OAuth apps (Clerk's shared development credentials don't work in production), and its database started empty instead of copying the development quizzes.

## Consequences

- The same person has a different user id in every Clerk instance. Moving quizzes between instances (or to another login provider) means rewriting `quizzes.owner_id`, otherwise they belong to nobody and only show up through Discover if published.
