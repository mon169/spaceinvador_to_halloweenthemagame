package org.newdawn.spaceinvaders.shop;
import org.newdawn.spaceinvaders.entity.ShipEntity;
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
            public void applyEffect(ShipEntity ship) {
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
            public void applyEffect(ShipEntity ship) {
                ship.setMoveSpeed(ship.getMoveSpeed() * 1.1);
            }
        });

        // 방어력 증가
        itemsForSale.add(new Item(
            "방어 실드",
            "방어력을 2 증가시킵니다.",
            300
        ) {
            @Override
            public void applyEffect(ShipEntity ship) {
                ship.increaseDefense(2);
            }
        });

        // 공격력 증가
        itemsForSale.add(new Item(
            "레이저 강화",
            "공격력을 1 증가시킵니다.",
            250
        ) {
            @Override
            public void applyEffect(ShipEntity ship) {
                ship.increaseAttackPower(1);
            }
        });

        // 최대 체력 증가
        itemsForSale.add(new Item(
            "방어막 강화",
            "최대 체력을 20 증가시킵니다.",
            350
        ) {
            @Override
            public void applyEffect(ShipEntity ship) {
                ship.increaseMaxHealth(20);
            }
        });

        // 체력 회복
        itemsForSale.add(new Item(
            "수리 키트",
            "체력을 50 회복합니다.",
            200
        ) {
            @Override
            public void applyEffect(ShipEntity ship) {
                ship.heal(50);
            }
        });

        // 폭탄 아이템 추가
        itemsForSale.add(new Item(
            "폭탄",
            "광역 공격을 할 수 있는 폭탄을 얻습니다(B키로 사용)",
            200
        ) {
            @Override
            public void applyEffect(ShipEntity ship) {
                ship.giveBomb();
            }
        });
        
        // 얼음 공격 아이템 추가
        itemsForSale.add(new Item(
            "얼음 무기",
            "적을 잠시 얼릴 수 있는 무기를 얻습니다(I키로 사용)",
            150
        ) {
            @Override
            public void applyEffect(ShipEntity ship) {
                ship.giveIceWeapon();
            }
        });
    }

    // 아이템 구매 로직
    public void purchaseItem(ShipEntity playerShip, int itemIndex) {
        if (itemIndex < 0 || itemIndex >= itemsForSale.size()) {
            System.out.println("잘못된 상품 번호입니다.");
            return;
        }

        Item selectedItem = itemsForSale.get(itemIndex);

        if (playerShip.getMoney() >= selectedItem.getCost()) {
            playerShip.spendMoney(selectedItem.getCost());
            playerShip.addItem(selectedItem);
            selectedItem.applyEffect(playerShip); // 아이템 효과 적용!
            System.out.printf("'%s' 구매를 완료했습니다!\n", selectedItem.getName());
        } else {
            System.out.println("💰 잔액이 부족합니다.");
        }
    }
    
    // 판매 아이템 목록을 반환하는 메서드 (UI 표시에 사용)
    public List<Item> getItemsForSale() {
        return new ArrayList<>(itemsForSale);
    }

    // 파일 이동 없이 코드 수정이 가능하도록 메서드 추가
    private void copyToList(ArrayList<Item> list) {
        list.addAll(itemsForSale);
    }
}
