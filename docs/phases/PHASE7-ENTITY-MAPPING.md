# Phase 7: Entity Field Mapping

## StudentAnswer Entity Mapping

| GradingService Code | Actual Entity Field | Type | Notes |
|---------------------|---------------------|------|-------|
| `score` | `pointsEarned` | BigDecimal | ⚠️ Need conversion |
| `maxScore` | `maxPoints` | BigDecimal | ⚠️ Need conversion |
| `feedback` | `teacherFeedback` | String | ✓ Rename only |
| `studentAnswer` | `answerText` / `answerJson` | String | ⚠️ Need logic to choose |
| `examQuestion` | NONE | - | ❌ No relationship, use `questionId` + `question` |

## ExamSubmission Entity Mapping

| GradingService Code | Actual Entity Field | Type | Notes |
|---------------------|---------------------|------|-------|
| `startTime` | `startedAt` | Timestamp | ✓ Rename only |
| `submitTime` | `submittedAt` | Timestamp | ✓ Rename only |
| `totalScore` | `totalScore` | BigDecimal | ⚠️ Need conversion |
| `maxScore` | `maxScore` | BigDecimal | ⚠️ Need conversion |
| `generalFeedback` | NONE | - | ❌ Need to add or use different field |

## Exam Entity - Need to Check

| Expected Field | Need to Verify |
|----------------|----------------|
| `duration` | Check actual field name |
| `showAnswersAfterSubmit` | Check actual field name |
| `passingScore` | Should exist |

## BigDecimal Conversion Strategy

```java
// Double → BigDecimal
BigDecimal bd = BigDecimal.valueOf(doubleValue);

// BigDecimal → Double
Double d = bigDecimal.doubleValue();

// Round BigDecimal to 2 decimal places
BigDecimal rounded = value.setScale(2, RoundingMode.HALF_UP);
```

## Action Plan

1. ✅ Document mapping
2. ⏳ Check Exam entity fields
3. ⏳ Fix GradingService (~50 occurrences)
4. ⏳ Fix GradingController
5. ⏳ Update DTOs if needed
6. ⏳ Compile & test

---
*Created: 21/11/2025 14:11*
*By: K24DTCN210-NVMANH*
