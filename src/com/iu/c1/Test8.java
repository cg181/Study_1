package com.iu.c1;

public class Test8 {
	public static void main(String [] args) {
		System.out.println("=== Test8 Start ===");
		
		//물건 값의 합계
		int price;
		//손님이 낸 돈 
		int money;
		//거스름 돈
		int don;
		
		//물건값의 합계보다 손님이 낸 돈이  더 많다라는 가정
		//1.물건값의 합계, 손님이 낸돈, 거스름돈 출력
		price = 3570;
		money = 100000;
		don = money-price;
		System.out.println("거스름돈 :" +don);
		//만원짜리 갯수를 담을 변수
		int man;
		man = don/10000;
		System.out.println("만원 : " +man +"장");
		//천원짜리 갯수를 담을 변수
		int cheon;
		cheon = don/1000%10;
		System.out.println("천원 : "+cheon +"장");
		//백원짜리
		int back;
		back = don/100%10;
		System.out.println("백원 : "+back +"개");
		//십원짜리
		int sip;
		sip = don/10%10;
		System.out.println("십원 : "+sip +"개");	
		
		//다른 방식
		man = don/10000;
		System.out.println(man);
		cheon =don%10000/1000;
		System.out.println(cheon);
		back = don%1000/100;
		System.out.println(back);
		sip = don%100/10;
		System.out.println(sip);

	
		//만원 ??장
		//천원 ??장
		//백원 ??개
		//십원 ??개
	
		

	}

}
