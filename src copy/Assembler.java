import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class Assembler {
	public static int variablesUsed = 0;
	public static void main(String[] args) {
		Hashtable<String,Integer> symbolTable = new Hashtable<String,Integer>();
		symbolTable.put("R0",0);
		symbolTable.put("R1",1);
		symbolTable.put("R2",2);
		symbolTable.put("R3",3);
		symbolTable.put("R4",4);
		symbolTable.put("R5",5);
		symbolTable.put("R6",6);
		symbolTable.put("R7",7);
		symbolTable.put("R8",8);
		symbolTable.put("R9",9);
		symbolTable.put("R10",10);
		symbolTable.put("R11",11);
		symbolTable.put("R12",12);
		symbolTable.put("R13",13);
		symbolTable.put("R14",14);
		symbolTable.put("R15",15);
		symbolTable.put("SCREEN",16384);
		symbolTable.put("KBD",24576);
		symbolTable.put("SP",0);
		symbolTable.put("LCL",1);
		symbolTable.put("ARG",2);
		symbolTable.put("THIS",3);
		symbolTable.put("THAT",4);

		Scanner user_input = new Scanner(System.in);
		System.out.print("Name of file to assemble (without .asm): ");
		String fileName = user_input.next();
		user_input.close();
		
		ArrayList<String> toTranslate = getCommandsToTranslate(fileName);
		toTranslate = removeComments(toTranslate);
		
		symbolTable = firstPass(toTranslate, symbolTable);

		ArrayList<String> translated = secondPass(symbolTable,toTranslate);
		
		writeHackFile(fileName,translated);	
	}
	public static ArrayList<String> removeComments(ArrayList<String>coms){
		ArrayList<String>m=new ArrayList<String>();
		for(String c:coms){
			int ci = c.indexOf('/');
			if (ci != -1){
				c = c.substring(0,ci);
			}
			c = c.replaceAll("\\s","");
			m.add(c);
		}
		return m;
	}
	public static ArrayList<String> secondPass(Hashtable<String,Integer>syms,ArrayList<String>toTranslate){
		ArrayList<String>translated=new ArrayList<String>();
		for(String c:toTranslate){
			if (c.startsWith("@"))
			{
				String sym = c.substring(1,c.length());
				if(!sym.matches("[0-9]+")){
					if(!syms.containsKey(sym)){
						syms.put(sym, 16+variablesUsed);
						variablesUsed++;
					}
					translated.add(translateAInstruction(syms.get(sym).toString()));
				}else{// command is @(number)
					translated.add(translateAInstruction(sym));
				}
			}
			else if (!c.startsWith("("))
			{
				translated.add(translateCInstruction(c));
			}
		}
		return translated;
	}
	public static String translateAInstruction(String t){
		String bin = Integer.toBinaryString(0x10000 | Integer.parseInt(t)).substring(1);
		return bin;
	}
	public static String translateCInstruction(String t){
		String a = "";
		String dest = "";
		String comp = "";
		String jump = "";
		// leftOfEqualSign=rightOfEqualSign;rightofSemiColon
		int indexOfEqualSign = t.indexOf('=');
		int indexOfSemiColon = t.indexOf(';');
		
		String rightOfSemiColon = "";
		if (indexOfSemiColon != -1){
			rightOfSemiColon = t.substring(indexOfSemiColon+1,t.length());
		}
		
		String leftOfEqualSign = "";
		if (indexOfEqualSign != -1){
			leftOfEqualSign = t.substring(0, indexOfEqualSign);
		}
		
		String compToTranslate = "";
		if (indexOfSemiColon == -1 && indexOfEqualSign != -1){
			compToTranslate = t.substring(indexOfEqualSign+1,t.length());
		}else if (indexOfSemiColon != -1 && indexOfEqualSign != -1){
			compToTranslate = t.substring(indexOfEqualSign+1,indexOfSemiColon);
		}else if (indexOfSemiColon != -1 && indexOfEqualSign == -1){
			compToTranslate = t.substring(0,indexOfSemiColon);
		}
		//finish defining parts
		//deal with JUMP
			if (rightOfSemiColon.equals("JGT")){
				jump = "001";
			}else if (rightOfSemiColon.equals("JEQ")){
				jump = "010";
			}else if (rightOfSemiColon.equals("JGE")){
				jump = "011";
			}else if (rightOfSemiColon.equals("JLT")){
				jump = "100";
			}else if (rightOfSemiColon.equals("JNE")){
				jump = "101";
			}else if (rightOfSemiColon.equals("JLE")){
				jump = "110";
			}else if (rightOfSemiColon.equals("JMP")){
				jump = "111";
			}else if (rightOfSemiColon.equals("")){
				jump = "000";
			}
		//end JUMP	
		//deal with DEST
		if (indexOfEqualSign == -1){
			dest = "000";
		} else {
			String d1 = "0";
			String d2 = "0";
			String d3 = "0";
			if (leftOfEqualSign.contains("A")){
				d1 = "1";
			}
			if (leftOfEqualSign.contains("D")){
				d2 = "1";
			}
			if (leftOfEqualSign.contains("M")){
				d3 = "1";
			}
			dest=d1+d2+d3;
		}
		//end DEST
		//deal with COMP
			if (compToTranslate.equals("0")){
				comp = "101010";
			}else if (compToTranslate.equals("1")){
				comp = "111111";
			}else if (compToTranslate.equals("-1")){
				comp = "111010";
			}else if (compToTranslate.equals("D")){
				comp = "001100";
			}else if (compToTranslate.equals("A")||compToTranslate.equals("M")){
				comp = "110000";
			}else if (compToTranslate.equals("!D")){
				comp = "001101";
			}else if (compToTranslate.equals("!A")||compToTranslate.equals("!M")){
				comp = "110001";
			}else if (compToTranslate.equals("-D")){
				comp = "001111";
			}else if (compToTranslate.equals("-A")||compToTranslate.equals("-M")){
				comp = "110011";
			}else if (compToTranslate.equals("D+1")){
				comp = "011111";
			}else if (compToTranslate.equals("A+1")||compToTranslate.equals("M+1")){
				comp = "110111";
			}else if (compToTranslate.equals("D-1")){
				comp = "001110";
			}else if (compToTranslate.equals("A-1")||compToTranslate.equals("M-1")){
				comp = "110010";
			}else if (compToTranslate.equals("D+A")||compToTranslate.equals("D+M")){
				comp = "000010";
			}else if (compToTranslate.equals("D-A")||compToTranslate.equals("D-M")){
				comp = "010011";
			}else if (compToTranslate.equals("A-D")||compToTranslate.equals("M-D")){
				comp = "000111";
			}else if (compToTranslate.equals("D&A")||compToTranslate.equals("D&M")){
				comp = "000000";
			}else if (compToTranslate.equals("D|A")||compToTranslate.equals("D|M")){
				comp = "010101";
			}
			if(compToTranslate.contains("M")){
				a = "1";
			}else{
				a = "0";
			}
		//end COMP
		return "111"+a+comp+dest+jump;
	}
	public static Hashtable<String,Integer> firstPass(ArrayList<String>commands, Hashtable<String,Integer>syms){
		int i = 0;
		for (String command:commands){
			if (command.startsWith("(")){
				syms.put(command.substring(1,command.length()-1), i);
			}else{
				i++;
			}
		}
		return syms;
	}
	public static void printSymbolTable(Hashtable<String,Integer>syms){
		for (String key : syms.keySet()) {
		    System.out.println(key + ":" + syms.get(key)+" ");
		}
	}
	public static ArrayList<String> getCommandsToTranslate(String fileName){
		File inputFile = new File ("./src/"+fileName+".asm");
		ArrayList<String> toTranslate = new ArrayList<String>();
		try {
			Scanner sc = new Scanner(inputFile);
			while (sc.hasNext()){
				String s = sc.nextLine();
				if (!s.startsWith("/") && !s.isEmpty()){
					toTranslate.add(s);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}	
		return toTranslate;
	}
	public static void writeHackFile(String fileName, ArrayList<String> binary){
	//PRINT to .hack file
				try {
					PrintWriter writer = new PrintWriter("./src/My"+fileName+".hack", "UTF-8");
					for(String s:binary){		
						writer.println(s);
					}
					writer.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}
}