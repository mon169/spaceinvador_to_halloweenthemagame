# 리팩토링 전 메트릭 측정 (Before)

**측정 일시:** 2025-11-27  
**대상:** 2차과제 현재 상태 (리팩토링 전)

---

## 📊 소프트웨어 메트릭

### 1. **Lines of Code (LOC)**
- **총 코드 라인:** 5,956 LOC
- Java 파일 수: 48개

### 2. **Number of Classes**
- 추정: 약 35-40개 클래스

### 3. **Cyclomatic Complexity**
- **평균 복잡도:** 추정 8-10
- **최고 복잡도:** Game.java (17 이상)
- Game.java의 `gameLoop()`: Complexity 17 (권장: 14 이하)
- Game.java의 특정 메서드: Cognitive Complexity 33 (권장: 15 이하)

### 4. **Code Quality Issues (정적 분석)**
- **총 이슈:** 89개 (15개 파일)
- **Critical/Major:** 약 25-30개
  - Null pointer risk: 1개
  - Raw type (제네릭 미사용): 4개
  - System.out/err 사용: ~20개
  - High complexity: 2개
- **Minor:** 약 60개
  - Unused imports: 5개
  - Unused fields: 13개
  - Serializable 경고: ~15개
  - Commented code: ~22개
  - 기타: ~10개

### 5. **Code Smells**
- **Commented-out code blocks:** 22개 이상
- **Magic strings (중복 리터럴):** 여러 개
- **Empty blocks:** 1개
- **Unnecessary casts:** 2개

---

## 🎯 주요 개선 필요 영역

### **1. Game.java (가장 심각)**
- Cognitive Complexity 33 (권장: 15)
- "Brain Method" 검출
- System.out/err 남용 (약 20회)
- 주석 처리된 코드 다수

### **2. SpriteStore.java**
- Null pointer 위험
- Raw type HashMap (제네릭 미사용)

### **3. Manager 클래스들**
- Unused fields (game, uiManager 등)
- System.err 사용

### **4. Boss 클래스들**
- Unused sprite fields

---

## 📝 비고
- 정적 분석 도구: SonarQube for IDE + Java 기본 린터
- Brain Method: 메서드가 너무 길고 복잡함 (LOC 70, Complexity 17)
- 주석 처리된 코드가 많아 유지보수성 저하






