
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Stack;

public class NestedReader {

	private BufferedReader stdin;
	private StringBuffer stringBuilder;
	private Stack stack;


	public NestedReader(BufferedReader stdin){
		this.stdin = stdin;
		this.stringBuilder = new StringBuffer();
		this.stack = new Stack();
	}


	public String getNestedString() throws IOException{
		char currChar;
		while (true) {
			currChar = (char)stdin.read();
			if (currChar == (char)-1){
				stdin.close();
				return stringBuilder.toString();
			}

			if (currChar == '\n'){
				break;
			}

			switch(currChar){
				case '{':
					//pop on stack at the stackCounter
					stack.push(currChar);
					stringBuilder.append(currChar);
					break;
				case '(':
					//pop on stack at the stackCounter
					stack.push(currChar);
					stringBuilder.append(currChar);
					break;
				case '}':
					//pop off top of stack
					if (stack.size() == 0){
						return stringBuilder.toString();
					} if ((char)stack.peek() != '{'){
						return stringBuilder.toString();
					}
					stack.pop();
					stringBuilder.append(currChar);
					break;
				case ')':
					//pop off top of stack
					//check to see if it's the same type of symbol. if not, error.
					if (stack.size() == 0){
						return stringBuilder.toString();
					} if ((char)stack.peek() != '('){
						return stringBuilder.toString();
					}
					stack.pop();
					stringBuilder.append(currChar);
					break;
				case '/':
					//there is a comment so ignore the comments
					char checkChar = (char)stdin.read();
					if (checkChar == '/'){
						stdin.readLine();
					}
					else {
						stringBuilder.append('/');
						stringBuilder.append(checkChar);
					}
					break;

				default:
					//add character to stringBuilder
					stringBuilder.append(currChar);
					break;
			}
		}


		if (stack.isEmpty()) {
			String value = stringBuilder.toString();
			stringBuilder.setLength(0);
			return value;
		} else {
			return getNestedString();
		}

	}


}
