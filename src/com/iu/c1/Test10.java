package com.iu.c1;

import java.util.Scanner;

public class Test10 {
	public static void main(String [] args ) {
		System.out.println("=== Test10 Start ===");
		// 941223-1234567
		
		// 0   -> 0
		// 1   -> 1
		// 2   -> 2
		// 3   -> 3
		// 4   -> 10
		// 7   -> 13
		// 8   -> 20
		// 12  -> 30 
		// 13  -> 31
		
		//4, 10 관련
		//java는 필요할 때 변수 선언 가능
		
		//실행 중에 데이터를 키보드로 부터 입력받을 준비
		Scanner sc = new Scanner(System.in);
		
		
		int input=3;
		int output=0; //11
		
		System.out.println("숫자를 입력하세요");
		input = sc.nextInt();
		output = input/4*10+input%4 ;
		
		System.out.println("input : " + input);
		System.out.println("output : " + output);


	}

}
