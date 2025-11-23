package org.newdawn.spaceinvaders.shop;
import org.newdawn.spaceinvaders.entity.UserEntity;
import java.util.ArrayList;
import java.util.List;

public class Shop {
    private List<Item> itemsForSale;

    public Shop() {
        this.itemsForSale = new ArrayList<>();
        initializeItems();
    }

    public void initializeItems() {
        // 공격 속도 증가
        itemsForSale.add(new Item(
            "냄새 발사기",
            "마늘 탄환 발사 속도가 10% 상승!\n오늘도 마을의 평화를 지켜라!",
            200
        ) {
            @Override
            public void applyEffect(UserEntity ship) {
                ship.setFiringInterval((long)(ship.getFiringInterval() * 0.9));
            }
        });

        // 이동 속도 증가
        itemsForSale.add(new Item(
            "호박 로켓 슈즈",
            "호박 모양 추진기로 이동 속도가 10% 증가!\nTrick보다 Treat가 빠르다!",
            150
        ) {
            @Override
            public void applyEffect(UserEntity ship) {
                ship.setMoveSpeed(ship.getMoveSpeed() * 1.1);
            }
        });

        // 방어력 증가
        itemsForSale.add(new Item(
            "카라멜 코팅 슈트",
            "부서져도 달콤한 방어력 +2!",
            300
        ) {
            @Override
            public void applyEffect(UserEntity ship) {
                ship.increaseDefense(2);
            }
        });

        // 공격력 증가
        itemsForSale.add(new Item(
            "강력한 마늘",
            "더욱 매운 마늘로 공격력이 1 증가!",
            250
        ) {
            @Override
            public void applyEffect(UserEntity ship) {
                ship.increaseAttackPower(1);
            }
        });

        // 최대 체력 증가
        itemsForSale.add(new Item(
            "쿠키 에너지바",
            "먹으면 용기가 생겨! 체력이 +20 증가.",
            350
        ) {
            @Override
            public void applyEffect(UserEntity ship) {
                ship.increaseMaxHealth(20);
            }
        });

        // 폭탄 아이템 추가
        itemsForSale.add(new Item(
            "생강 폭탄",
            "터지면 생강 냄새로 광역 피해를 입힌다!\n(B키로 사용)",
            200
        ) {
            @Override
            public void applyEffect(UserEntity ship) {
                ship.giveBomb();
            }
        });
        
        // 얼음 공격 아이템 추가
        itemsForSale.add(new Item(
            "박하사탕",
            "박하의 마력을 담은 사탕. 달콤함 뒤엔\n냉기의 칼날이 숨어있다.(I키로 사용)",
            150
        ) {
            @Override
            public void applyEffect(UserEntity ship) {
                ship.giveIceWeapon();
            }
        });
        
        // 에너지 실드 아이템 추가 
        itemsForSale.add(new Item(
            "거미줄 보호막",
            "요새를 보호하는 방어막입니다. 구매 후 S키를 눌러\n요새 방어막을 활성화할 수 있습니다.\n5초 동안 무적 상태가 됩니다.",
            400
        ) {
            @Override
            public void applyEffect(UserEntity ship) {
                ship.giveShield(); // 방어막을 인벤토리에 추가
            }
        });
    }

    // 아이템 구매 로직
    public void purchaseItem(UserEntity playerShip, int itemIndex) {
        System.out.println("🛒 purchaseItem 호출: itemIndex=" + itemIndex + ", itemsForSale.size()=" + itemsForSale.size());
        
        if (itemIndex < 0 || itemIndex >= itemsForSale.size()) {
            System.out.println("❌ 잘못된 상품 번호입니다. (인덱스: " + itemIndex + ", 범위: 0-" + (itemsForSale.size() - 1) + ")");
            return;
        }

        Item selectedItem = itemsForSale.get(itemIndex);
        int currentMoney = playerShip.getMoney();
        int itemCost = selectedItem.getCost();
        
        System.out.println("💰 구매 시도: " + selectedItem.getName() + " (가격: " + itemCost + "골드, 보유: " + currentMoney + "골드)");

        if (currentMoney >= itemCost) {
            playerShip.spendMoney(itemCost);
            playerShip.addItem(selectedItem);
            
            // 구매 전 상태 로그
            int oldShieldCount = playerShip.getShieldCount();
            boolean oldHasShield = playerShip.hasShield();
            
            selectedItem.applyEffect(playerShip); // 아이템 효과 적용!
            
            // 구매 후 상태 로그
            int newShieldCount = playerShip.getShieldCount();
            boolean newHasShield = playerShip.hasShield();
            
            System.out.println("✅ '" + selectedItem.getName() + "' 구매 완료! (남은 골드: " + playerShip.getMoney() + ")");
            if (selectedItem.getName().equals("거미줄 보호막") || selectedItem.getName().equals("방어막")) {
                System.out.println("🛡 방어막 구매 확인 - 이전: " + oldShieldCount + " (hasShield=" + oldHasShield + ") → 이후: " + newShieldCount + " (hasShield=" + newHasShield + ")");
            }
        } else {
            System.out.println("❌ 잔액이 부족합니다. (필요: " + itemCost + "골드, 보유: " + currentMoney + "골드)");
        }
    }
    
    // 판매 아이템 목록을 반환하는 메서드 (UI 표시에 사용)
    public List<Item> getItemsForSale() {
        return new ArrayList<>(itemsForSale);
    }
}
