# fp-stonadskonto

Business-rule library for stonadskonto and minsterett calculation.

## Shared context

- Source of truth for shared domain, architecture, and conventions: `navikt/fp-context`
- Copilot Space: `navikt/TeamForeldrepenger`

## Repo-specific context

| Topic     | Details                                                                    |
|-----------|----------------------------------------------------------------------------|
| Role      | Calculates benefit quotas and related rights for foreldrepenger flows      |
| Consumers | `fp-sak` (uttak-flow), `fp-grunndata` (for self-service apps)              |
| Tech stack  | Java, `fp-nare` rule framework                                             |
| Structure | Single SemVer library with `regelmodell/` and public API in `grensesnitt/` |

Pure stateless calculation based on input data.

## Verification

- Treat `grensesnitt/` as breaking API surface.
- Verify behavior changes through `fp-sak` and relevant `navikt/fp-autotest` flows.
