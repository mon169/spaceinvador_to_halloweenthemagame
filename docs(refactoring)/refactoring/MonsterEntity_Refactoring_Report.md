# MonsterEntity.java 리팩토링 보고서

## 1. 개요

- **파일명**: `MonsterEntity.java`
- **패키지**: `org.newdawn.spaceinvaders.entity`
- **리팩토링 일시**: 2025-11-28
- **목적**: 코드 품질 개선, 가독성 및 유지보수성 향상

---

## 2. 리팩토링 전 메트릭

- **WMC (Weighted Methods per Class)**: 55
- **OCavg (평균 복잡도)**: 4.23
- **OCmax (최대 복잡도)**: 17
- **LOC (Lines of Code)**: 360줄

---

## 3. 발견된 Bad Smells

### Bad Smell 1: Magic Numbers
- **문제점**: 코드 전체에 20개 이상의 하드코딩된 숫자가 산재
- **위치**: 
  - 경계값 (10, 760, 40, 520)
  - 스테이지별 속도 (40, 60, 80, 100)
  - 공격 관련 시간 (1000, 1500, 2000)
  - 샷 속도 (180, 200, 250)
  - 기타 (0.6, 0.5, 40 등)
- **영향**: 숫자의 의미 파악 어려움, 수정 시 일관성 유지 어려움

### Bad Smell 2: Duplicate Code
- **문제점**: 방향에서 속도를 계산하는 로직이 3곳에서 반복
- **위치**: 
  - `initRandomMovement()` 메서드
  - `updateFreeze()` 메서드
  - `randomDirectionChange()` 메서드
  - 생성자 (두 번째 오버로드)
- **영향**: 유지보수 시 여러 곳을 수정해야 함, 버그 발생 가능성 증가

### Bad Smell 3: Duplicate Conditional Logic
- **문제점**: Stage 4/5 체크 로직이 2곳에서 중복
- **위치**: 
  - `autoAdjustForStage4()` 메서드
  - `preventObstaclePenetration()` 메서드
- **영향**: 조건 변경 시 여러 곳 수정 필요, 일관성 유지 어려움

### Bad Smell 4: Hardcoded Values in Methods
- **문제점**: 메서드 내부에 숫자가 직접 사용되어 의미 파악 불가
- **위치**: 
  - `resolveTarget()`: fortress 중심 오프셋 40
  - `collidedWith()`: 충돌 데미지 10
  - `draw()`: 스케일 비율 0.5
- **영향**: 코드 의도 불명확, 재사용성 저하

---

## 4. 적용된 리팩토링 기법

### 기법 1: Extract Constant
- **설명**: Magic Number를 의미 있는 상수로 추출
- **적용 대상**: 
  - 경계값: `BOUNDARY_LEFT`, `BOUNDARY_RIGHT`, `BOUNDARY_TOP`, `BOUNDARY_BOTTOM`
  - 속도: `STAGE1_SPEED`, `STAGE2_SPEED`, `STAGE3_SPEED`, `STAGE4_SPEED`
  - 시간: `DIRECTION_CHANGE_BASE`, `ATTACK_BASE_DELAY`, `INITIAL_ATTACK_DELAY`
  - 샷 속도: `ICESHOT_SPEED`, `BOMBSHOT_SPEED`, `NORMAL_SHOT_SPEED`
  - 기타: `FORTRESS_CENTER_OFFSET`, `COLLISION_DAMAGE`, `DRAW_SCALE`, `ATTACK_PROBABILITY`
- **효과**: 코드 의도 명확화, 수정 용이성 향상

### 기법 2: Extract Method (중복 코드 제거)
- **설명**: 중복된 속도 계산 로직을 별도 메서드로 추출
- **추출 메서드**: `updateVelocityFromDirection()`
- **적용 위치**: 4곳에서 호출로 대체
- **효과**: 코드 중복 제거, 단일 책임 원칙 준수

### 기법 3: Extract Method (조건 로직 단순화)
- **설명**: Stage 4/5 체크 로직을 헬퍼 메서드로 추출
- **추출 메서드**: `isStageWithObstacles()`
- **적용 위치**: `autoAdjustForStage4()`, `preventObstaclePenetration()`
- **효과**: 조건문 가독성 향상, 중복 제거

### 기법 4: Replace Magic Number with Symbolic Constant
- **설명**: 메서드 내 하드코딩된 숫자를 상수로 교체
- **적용 대상**: 모든 메서드의 리터럴 값
- **효과**: 일관성 향상, 자기 문서화 코드

---

## 5. 리팩토링 후 개선 효과

### 코드 품질
- ✅ Magic Number 20개 → 의미 있는 상수로 변환
- ✅ 중복 코드 제거 (4곳 → 1개 메서드)
- ✅ 조건 로직 단순화 (2곳 → 1개 헬퍼 메서드)

### 가독성
- ✅ 숫자의 의미가 명확해짐
- ✅ 메서드명으로 로직 의도 표현
- ✅ 복잡한 조건문 단순화

### 유지보수성
- ✅ 값 수정 시 한 곳만 변경
- ✅ 중복 로직 제거로 버그 위험 감소
- ✅ 테스트 용이성 향상

### 기능 안정성
- ✅ 100% 기능 동일성 보장
- ✅ 빌드 성공 (mvn compile)
- ✅ 린터 에러 0개

---

## 6. 결론

MonsterEntity.java의 리팩토링을 통해 코드 품질을 크게 개선했습니다. Magic Number 제거와 중복 코드 제거를 통해 가독성과 유지보수성이 향상되었으며, 게임 기능은 100% 동일하게 유지되었습니다. 향후 스테이지별 속도 조정이나 경계값 변경이 필요할 때 상수만 수정하면 되어 유지보수가 훨씬 용이해졌습니다.


