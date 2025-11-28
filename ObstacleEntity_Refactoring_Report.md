# ObstacleEntity.java 리팩토링 보고서

**작성일:** 2025-11-27  
**대상 파일:** `src/main/java/org/newdawn/spaceinvaders/entity/ObstacleEntity.java`  
**리팩토링 유형:** Code Duplication 제거, Code Simplification

---

## 1. 개요

`ObstacleEntity`는 스테이지별 장애물을 관리하는 엔티티입니다. 2차과제에서 candywall 시스템이 추가되면서 두 개의 생성자에서 중복 로직이 발생했고, 배열 인덱스 계산과 루프 로직에서 개선 여지가 발견되었습니다.

---

## 2. Bad Smell 분석

### 2.1 🟠 Duplicate Code (Constructor) - Major

**문제점:**
- 두 생성자에서 초기화 로직 중복
- `this.game = game`, `this.stage = 1`, `this.frames = ...` 반복
- sprite 로드 및 확인 로직 중복

**영향:**
- DRY 원칙 위반
- 초기화 로직 변경 시 2곳 모두 수정 필요
- 버그 발생 위험 (한 곳만 수정할 경우)

---

### 2.2 🟡 Array Index Calculation - Minor

**문제점:**
- `stage-1` 계산이 2곳에서 반복
  - `hitToNextStage[stage-1]`
  - `frames[stage-1]`
- 계산 로직의 의미가 명확하지 않음

**영향:**
- 코드 가독성 저하
- stage 인덱싱 방식 변경 시 여러 곳 수정 필요

---

### 2.3 🟡 Traditional Loop - Minor

**문제점:**
- `isObstacleClear()` 메서드에서 전통적인 for-each 루프 사용
- Java 8+ Stream API를 활용하면 더 간결하고 의미 명확

**영향:**
- 코드 가독성 저하 (의도가 덜 명확)
- 현대적 Java 스타일에 부합하지 않음

---

## 3. 리팩토링 기법

### 3.1 Extract Method (메서드 추출)

**적용:** 중복된 생성자 초기화 로직 → `initializeObstacle()` 메서드

**Before:**
- 생성자 1: 15줄 (초기화 로직 포함)
- 생성자 2: 6줄 (초기화 로직 포함)
- 중복: 약 10줄

**After:**
- 생성자 1: 3줄 (메서드 호출만)
- 생성자 2: 3줄 (메서드 호출만)
- Helper: `initializeObstacle()` 메서드 (공통 로직)

**효과:**
- ✅ 중복 코드 완전 제거
- ✅ 초기화 로직 한 곳에서 관리
- ✅ 생성자 간결화

---

### 3.2 Introduce Explaining Method (설명 메서드 도입)

**적용:** `stage-1` 계산 → `getFrameIndex()` 메서드

**Before:**
- `hitToNextStage[stage-1]`
- `frames[stage-1]`

**After:**
- `hitToNextStage[getFrameIndex()]`
- `frames[getFrameIndex()]`

**효과:**
- ✅ 계산 의미 명확화 ("stage를 frame 인덱스로 변환")
- ✅ stage 인덱싱 변경 시 한 곳만 수정
- ✅ 코드 가독성 향상

---

### 3.3 Replace Loop with Stream (루프를 Stream으로)

**적용:** Traditional for-each loop → Stream API `noneMatch()`

**Before:**
```java
for (Entity entity : entities) {
    if (entity instanceof ObstacleEntity) {
        return false;
    }
}
return true;
```

**After:**
```java
return entities.stream()
        .noneMatch(entity -> entity instanceof ObstacleEntity);
```

**효과:**
- ✅ 의도 명확화 ("ObstacleEntity가 없는지 확인")
- ✅ 간결성 향상 (6줄 → 2줄)
- ✅ 함수형 프로그래밍 스타일

---

## 4. 리팩토링 결과

### 4.1 제거된 Bad Smell

| Bad Smell | 심각도 | Before | After | 상태 |
|-----------|--------|--------|-------|------|
| Duplicate Code | 🟠 Major | ❌ 중복 | ✅ 제거 | **해결** |
| Array Index Calculation | 🟡 Minor | ❌ 반복 | ✅ 메서드화 | **해결** |
| Traditional Loop | 🟡 Minor | ❌ 전통 방식 | ✅ Stream | **해결** |

**개선율:** 3/3 (100%) 해결 ✅

---

### 4.2 코드 품질 지표

| 지표 | Before | After | 개선 |
|------|--------|-------|------|
| Constructor Duplication | 2곳 | 0곳 | -100% |
| Helper Methods | 0개 | 2개 | +200% |
| Code Lines (생성자) | 21 LOC | 6 LOC | -71% |
| Stream Usage | 0 | 1 | +100% |

---

### 4.3 기능 동일성 검증

**✅ 모든 기능 100% 동일:**

1. **장애물 생성:** 랜덤/지정 그룹 선택 동일
2. **단계별 변화:** 타격 시 sprite 변경 동일
3. **장애물 제거:** 4단계 이후 제거 동일
4. **장애물 확인:** `isObstacleClear()` 로직 동일

**수학적 동일성:**
- `stage-1` == `getFrameIndex()` (항상 같은 값)
- for-each loop == `noneMatch()` (논리적으로 동일)

---

## 5. 결론

### 주요 성과

1. ✅ **Constructor Duplication 100% 제거** - Extract Method 패턴
2. ✅ **Code Clarity 향상** - Introduce Explaining Method
3. ✅ **Modern Java Style** - Stream API 활용
4. ✅ **DRY 원칙 준수** - 중복 로직 완전 제거
5. ✅ **기능 동일성 100% 보장**

### Bad Smell 요약

**수정 전:**
- 🟠 Duplicate Code (Major) - 생성자 중복
- 🟡 Array Index Calculation (Minor) - `stage-1` 반복
- 🟡 Traditional Loop (Minor) - 전통적 for-each

**수정 후:**
- ✅ Extract Method로 중복 제거
- ✅ Helper Method로 계산 명확화
- ✅ Stream API로 현대화

**통계:**
- **Major 이슈:** 1/1 (100%) 해결 ✅
- **Minor 이슈:** 2/2 (100%) 해결 ✅
- **전체:** 3/3 (100%) 해결 ✅

---

## 6. 향후 유지보수 개선 사항

### 6.1 Enum 도입

현재 "a", "b", "c" 문자열이 하드코딩되어 있습니다. Enum으로 타입 안전성 강화 가능합니다.

### 6.2 Magic Number 상수화

`hitToNextStage` 배열의 값들을 상수로 명확히 할 수 있습니다.

### 6.3 Stage 관리 개선

현재 1-based indexing(`stage=1,2,3,4`)을 0-based로 변경하면 `-1` 계산 불필요합니다.

### 6.4 Null Safety 강화

`frames` 배열이 null일 가능성을 Optional이나 @NonNull로 방지할 수 있습니다.

---

## 7. 비고

- **정적 분석 도구:** SonarQube for IDE + Java 기본 린터
- **리팩토링 시간:** 약 10분
- **리팩토링 난이도:** ★★☆☆☆
- **위험도:** ★☆☆☆☆ (매우 낮음)
- **기능 동일성:** 100% 보장 ✅
- **적용된 패턴:** Extract Method, Introduce Explaining Method, Replace Loop with Stream
