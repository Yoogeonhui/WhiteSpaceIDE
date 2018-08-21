package org.dimigo.whitespaceide;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class Compiler extends Thread{
	StringBuilder command;
	StringBuilder parameter;
	String code, stdIn;
	char[] stdInToChar, codes;
	Stack<Integer> routines;
	Stack<Long> stack;
	boolean nowCommand= true;
	Context uiContext;
	Map<String, Integer> labels;
	Map<Long, Long> heap;
	Activity main;
	TextView output;
	boolean killed = false;

	int i, stdin_pointer = 0;

	public void kill(){
		killed = true;
	}

	public Compiler(String code, String stdIn, Context context){
		this.code = code;
		this.stdIn = stdIn;
		this.uiContext = context;
		stdInToChar = stdIn.toCharArray();
		heap = new HashMap<>();
		labels = new HashMap<>();
		routines = new Stack<>();
		stack = new Stack<>();
		Log.d("STDINLENGTH", "len: "+this.stdIn.length()+", chararray len: "+this.stdInToChar.length);
	}

	char convertChar(char input){
		char converted;
		switch(input){
			case 's': case 'S': case ' ': {
				converted = 'S';
				break;
			}
			case 't': case 'T': case '\t':{
				converted = 'T'; break;
			}
			case '\n': case 'l': case 'L':{
				converted = 'L'; break;
			}
			default:{
				converted = ' ';
			}
		}
		return converted;
	}

	void setLabels(){
		for(i=0;i<codes.length;i++){
			char now = codes[i];
			char converted = convertChar(now);
			try {
				if (converted != ' ') {
					if(nowCommand){
						command.append(converted);
						boolean dontClearflag = false;
						switch (command.toString()) {
							case "SS": case "STS": case "STL":
							case "LSS": case "LST": case "LSL": case "LTS": case "LTT": {
								nowCommand = false;
								dontClearflag = true;
								break;
							}
							case "SLS": case "SLT": case "SLL":
							//T no Param 사칙연산
							case "TSSS": case "TSST": case "TSSL": case "TSTS": case "TSTT":
							//Heap
							case "TTS": case "TTT":
							//Ls
							case "LTL": case "LLL":
							case "TLSS": case "TLST": case "TLTS": case "TLTT":
								break;
							default:
								dontClearflag = true;
						}
						if(!dontClearflag) {
							command = new StringBuilder();
						}
					}else{
						parameter.append(converted);
						char what_to_do = parameter.charAt(parameter.length()-1);
						if(what_to_do == 'L') {
							parameter.deleteCharAt(parameter.length() - 1);
							if (command.charAt(0) == 'L') {
								String param = parameter.toString();
								if(command.toString().equals("LSS")){
									labels.put(param, i);
								}
							}
							command = new StringBuilder();
							parameter = new StringBuilder();
							nowCommand = true;
						}
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				main.runOnUiThread(
						() -> {
							output.append("Error in Label Setting "+e.toString());
						}
				);
				return;
			}

		}
		for (String name: labels.keySet()){
			String key =name.toString();
			String value = labels.get(name).toString();
			Log.d("LABELS",key + " " + value);
		}
	}

	void runCommand() {
		boolean dontClearflag = false;
		switch (command.toString()) {
			case "SS": case "STS": case "STL":
			case "LSS": case "LST": case "LSL": case "LTS": case "LTT": {
				nowCommand = false;
				dontClearflag = true;
				break;
			}
			case "SLS": {
				long copynum = stack.peek();
				stack.push(copynum);
				break;
			}
			case "SLT": {
				long first = stack.peek();
				stack.pop();
				long second = stack.peek();
				stack.pop();
				stack.push(first);
				stack.push(second);
				break;
			}
			case "SLL": {
				stack.pop();
				break;
			}
			//T no Param 사칙연산
			case "TSSS": {
				long a = stack.peek();
				stack.pop();
				long b = stack.peek();
				stack.pop();
				stack.push(a + b);
				break;
			}
			case "TSST": {
				long a = stack.peek(); stack.pop(); long b = stack.peek(); stack.pop();
				stack.push(b-a);
				break;
			}
			case "TSSL":{
				long a = stack.peek(); stack.pop(); long b = stack.peek(); stack.pop();
				stack.push(b*a);
				break;
			}
			case "TSTS":{
				long a = stack.peek(); stack.pop(); long b = stack.peek(); stack.pop();
				stack.push(b/a);
				break;
			}
			case "TSTT":{
				long a = stack.peek(); stack.pop(); long b = stack.peek(); stack.pop();
				stack.push(b%a);
				break;
			}
			//Heap
			case "TTS":{
				long v = stack.peek(); stack.pop(); long a = stack.peek(); stack.pop();
				heap.put(a, v);
				break;
			}
			case "TTT":{
				long a = stack.peek(); stack.pop();
				stack.push(heap.get(a));
				break;
			}
			//Ls
			case "LTL":
			{
				if(routines.size()>0){
					int where_to_go = routines.peek();
					routines.pop();
					i = where_to_go;
				}
				break;
			}

			case "LLL":
			{
				//program end.
				i = code.length();
				break;
			}

			case "TLSS":
			{
				// Stack 의 숫자 ASCII로 출력
				long a = stack.peek();
				stack.pop();
				// ASCII 변환
				char ch = (char)a;
				Log.d("TAG", "char: "+ch);
				main.runOnUiThread(()->{
					output.append(Character.toString(ch));
				});
				break;
			}
			case "TLST":
			{
				long a = stack.peek();
				stack.pop();
				Log.d("TAG", "print num Now a: "+a);
				main.runOnUiThread(()->{
					output.append(Long.toString(a));
				});
				break;
			}

			case "TLTS":
			{
				long a = stack.peek();
				stack.pop();
				Log.d("POINTER", stdin_pointer+"");
				char input;
				input = stdInToChar[stdin_pointer];
				stdin_pointer++;
				int ascii = (int)input;
				heap.put(a, (long)ascii);
				break;
			}
			case "TLTT":
			{
				//숫자 입력받기
				long a = stack.peek();
				stack.pop();
				int getmin = stdIn.length();
				int enter = stdIn.indexOf('\n', stdin_pointer), space = stdIn.indexOf(' ', stdin_pointer);
				if(enter < getmin && enter!=-1)
					getmin = enter;
				if(space < getmin && space!=-1)
					getmin = space;
				String what_to_convert = stdIn.substring(stdin_pointer, getmin);
				stdin_pointer = getmin;
				Long result;
				try {
					result = Long.parseLong(what_to_convert);
				}catch(NumberFormatException e){
					result = 0L;
				}
				heap.put(a, result);
				break;
			}
			default:
				dontClearflag = true;
		}
		if(!dontClearflag) {
			command = new StringBuilder();
		}
	}

	long STtoLong(String longString){
		char[] longChars = longString.toCharArray();
		long result = 0L;
		int cnt = 0;
		for(int i= longString.length()-1;i>=0;i--){
			if(longChars[i]=='T'){
				result += (1<<cnt);
			}
			cnt++;
		}
		return result;
	}

	void runParameter(){
		char what_to_do = parameter.charAt(parameter.length()-1);
		if(what_to_do == 'L'){
			parameter.deleteCharAt(parameter.length()-1);
			switch(command.charAt(0))
			{
				case 'S':
				{
					char pos_neg = parameter.charAt(0);
					parameter.deleteCharAt(0);
					String longString = parameter.toString();
					long inputNum = STtoLong(longString);
					if(pos_neg == 'T')
						inputNum *= -1;
					switch(command.toString()){
						case "SS":{
							stack.push(inputNum);
							break;
						}
						case "STS":{
							inputNum--;
							long copy = stack.get((int)inputNum);
							stack.push(copy);
							break;
						}
						case "STL":{
							long item = stack.peek();
							stack.pop();
							for(int i=0;i<inputNum;i++){
								stack.pop();
							}
							stack.push(item);
							break;
						}

					}
					break;
				}
				case 'L':
				{
					String param = parameter.toString();
					Log.d("REQUIRED",param+" index "+i);
					switch(command.toString()){
						case "LSS":{
							labels.put(param, i);
							break;
						}
						case "LST":{
							int to = labels.get(param);
							routines.push(i);
							i = to;
							break;
						}
						case "LSL":{
							i = labels.get(param);
							break;
						}
						case "LTS":{
							long stackNum = stack.peek();
							stack.pop();
							if(stackNum == 0){

								i=labels.get(param);
							}
							break;
						}
						case "LTT":{
							long stackNum = stack.peek();
							stack.pop();
							if(stackNum < 0){
								i=labels.get(param);
							}
							break;
						}
					}
					break;
				}
			}
			command = new StringBuilder();
			parameter = new StringBuilder();
			nowCommand = true;
		}
	}

	@Override
	public void run() {
		super.run();

		main = ((Activity)uiContext);
		command  = new StringBuilder("");
		parameter = new StringBuilder("");
		output = main.findViewById(R.id.stdOut);
		codes = code.toCharArray();
		setLabels();
		nowCommand = true;
		command  = new StringBuilder("");
		parameter = new StringBuilder("");
		//reinitialize
		for(i=0;i<codes.length && !killed;i++){
			char now = codes[i];
			char converted = convertChar(now);
			try {
				if (converted != ' ') {
					if (nowCommand) {
						command.append(converted);
						runCommand();
					} else {
						parameter.append(converted);
						runParameter();
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				main.runOnUiThread(
						() -> {
							output.setText(e.toString());
						}
				);
				return;
			}
		}
		killed = false;
	}

}