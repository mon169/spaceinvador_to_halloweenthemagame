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
            "연사 가속기",
            "미사일 발사 간격을 10% 감소시킵니다.",
            200
        ) {
            @Override
            public void applyEffect(UserEntity ship) {
                ship.setFiringInterval((long)(ship.getFiringInterval() * 0.9));
            }
        });

        // 이동 속도 증가
        itemsForSale.add(new Item(
            "고성능 부스터",
            "기체 이동 속도를 10% 증가시킵니다.",
            150
        ) {
            @Override
            public void applyEffect(UserEntity ship) {
                ship.setMoveSpeed(ship.getMoveSpeed() * 1.1);
            }
        });

        // 방어력 증가
        itemsForSale.add(new Item(
            "방어력 강화",
            "방어력을 2 증가시킵니다.",
            300
        ) {
            @Override
            public void applyEffect(UserEntity ship) {
                ship.increaseDefense(2);
            }
        });

        // 공격력 증가
        itemsForSale.add(new Item(
            "공격력 강화",
            "공격력을 1 증가시킵니다.",
            250
        ) {
            @Override
            public void applyEffect(UserEntity ship) {
                ship.increaseAttackPower(1);
            }
        });

        // 최대 체력 증가
        itemsForSale.add(new Item(
            "체력 강화",
            "최대 체력을 20 증가시킵니다.",
            350
        ) {
            @Override
            public void applyEffect(UserEntity ship) {
                ship.increaseMaxHealth(20);
            }
        });

        // 폭탄 아이템 추가
        itemsForSale.add(new Item(
            "막대 사탕",
            "광역 공격을 할 수 있는 사탕폭탄을 얻습니다(B키로 사용)",
            200
        ) {
            @Override
            public void applyEffect(UserEntity ship) {
                ship.giveBomb();
            }
        });
        
        // 얼음 공격 아이템 추가
        itemsForSale.add(new Item(
            "얼음 사탕",
            "적을 잠시 얼릴 수 있는 무기를 얻습니다(I키로 사용)",
            150
        ) {
            @Override
            public void applyEffect(UserEntity ship) {
                ship.giveIceWeapon();
            }
        });
        
        // 에너지 실드 아이템 추가 
        itemsForSale.add(new Item(
            "방어막",
            "요새를 보호하는 방어막입니다. 구매 후 S키를 눌러\n요새 방어막을 활성화할 수 있습니다.\n방어력 수치만큼 초 동안 지속됩니다.",
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
            if (selectedItem.getName().equals("방어막")) {
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
