# Implementation Plan - Import Players with Blank Team to Banco da Liga

Update `CsvRosterParser.kt` so that players with a prefilled name and a blank team column are not discarded, but rather assigned to the special club "Banco da Liga". Discarding will only occur when the name is blank.

## User Review Required

> [!IMPORTANT]
> - Players with blank team will now be assigned to `"Banco da Liga"` instead of being dropped as an error.
> - Lines with blank name will still be discarded and logged as errors.
> - Unit tests in `CsvRosterParserTest.kt` will be updated and expanded to cover this behavior.

## Proposed Changes

### Domain / Parser

#### [MODIFY] [CsvRosterParser.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/domain/parser/CsvRosterParser.kt)
- Modify validation logic:
  - If `name.isBlank()`, add error ("Dados incompletos (nome em branco).") and `continue`.
  - If `name.isNotBlank() && team.isBlank()`, assign `team = InitialDataDefaults.LEAGUE_BANK_NAME` ("Banco da Liga") so they are correctly routed to the League Bank during import.
  - If both `name` and `team` are present, use `team` normally.

### Tests

#### [MODIFY] [CsvRosterParserTest.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/test/java/com/example/legacymasterliga/app/src/test/java/com/example/legacymasterliga/domain/parser/CsvRosterParserTest.kt)
- Add unit test case: `"csv with blank team imports player into Banco da Liga"`
- Add unit test case: `"csv with blank name discards row and records error"`

## Verification Plan

### Automated Tests
- Run unit tests: `gradle_build("app:testDebugUnitTest")`

### Manual Verification
- Verify parse results and summary counts.
