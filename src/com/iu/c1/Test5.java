package com.iu.c1;

public class Test5 {
	public static void main(String [] args) {
		System.out.println("=== Test5 Start ===");
		
		int num1 = 3;
		long num2 = 9L;
		
		System.out.println(num1);
		
		num1 = (int)num2;
		System.out.println(num1);
		
		num1 =3;
		
		//num2 = (long)num1;
		num2 = num1;

		System.out.println(num2);
		
		char ch = 'a';
		int num3 = ch;
		System.out.println("num3 : "+num3);
		
		ch = (char)(num3+25);
		
		System.out.println("ch : "+ch);
		
		long num4 =10L;
		float f1 = num4;

		
		
	}
}
