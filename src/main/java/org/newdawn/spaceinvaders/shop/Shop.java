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
            "🧄 냄새 발사기",
            "마늘 탄환 발사 속도가 10% 상승! 오늘도 마을의 평화를 지켜라!",
            200
        ) {
            @Override
            public void applyEffect(UserEntity ship) {
                ship.setFiringInterval((long)(ship.getFiringInterval() * 0.9));
            }
        });

        // 이동 속도 증가
        itemsForSale.add(new Item(
            "🎃 호박 로켓 슈즈",
            "호박 모양 추진기로 이동 속도가 10% 증가! Trick보다 Treat가 빠르다!",
            150
        ) {
            @Override
            public void applyEffect(UserEntity ship) {
                ship.setMoveSpeed(ship.getMoveSpeed() * 1.1);
            }
        });

        // 방어력 증가
        itemsForSale.add(new Item(
            "🛡️ 카라멜 코팅 슈트",
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
            "🧄 강력한 마늘 🧄",
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
            "🍪 쿠키 에너지바",
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
            "💥 마늘 폭탄",
            "터지면 마늘 냄새로 광역 피해를 입힌다!\n(B키로 사용)",
            200
        ) {
            @Override
            public void applyEffect(UserEntity ship) {
                ship.giveBomb();
            }
        });
        
        // 얼음 공격 아이템 추가
        itemsForSale.add(new Item(
            "❄️ 민트 사탕",
            "입안이 시릴 정도의 냉기! 적들을 얼려버린다.\n(I키로 사용)",
            150
        ) {
            @Override
            public void applyEffect(UserEntity ship) {
                ship.giveIceWeapon();
            }
        });
        
        // 에너지 실드 아이템 추가 
        itemsForSale.add(new Item(
            "🕸️ 거미줄 보호막",
            "거미가 짠 달콤한 실로 만든 보호막! 적의 총알을 한 번 막아줍니다.\n(S키로 사용)",
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
        if (itemIndex < 0 || itemIndex >= itemsForSale.size()) {
            System.out.println("잘못된 상품 번호입니다.");
            return;
        }

        Item selectedItem = itemsForSale.get(itemIndex);

        if (playerShip.getMoney() >= selectedItem.getCost()) {
            playerShip.spendMoney(selectedItem.getCost());
            playerShip.addItem(selectedItem);
            selectedItem.applyEffect(playerShip); // 아이템 효과 적용
            System.out.printf("'%s' 구매를 완료했습니다!\n", selectedItem.getName());
        } else {
            System.out.println("잔액이 부족합니다.");
        }
    }
    
    // 판매 아이템 목록을 반환하는 메서드 (UI 표시에 사용)
    public List<Item> getItemsForSale() {
        return new ArrayList<>(itemsForSale);
    }
}
