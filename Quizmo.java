import java.io.InputStream;
import java.io.PrintStream;

public class Quizmo
{
  public static void main(String[] args)
  {
  	Quizmo agent = new Quizm(System.in, System.out);
	agent.host();
  }
  
  Scanner keyboard;
  PrintStream output;
  
	QuizParser parser;
	PriorityHeap queryQueue;
	
	//The number of the next question to ask.
	//Used to keep order in the queue.
	int orderIndex;
	//Indicate whether, after the last question, the user chose to exit or request retry.
	boolean userRedo, userExit;
  
  public Quizmo(InputStream input, PrintStream output, String testFile)
  {
    keyboard = new Scanner(input);
	this.output = output;
	parser = new QuizParser(testFile);
  }
  
  public void host()
  {
  	//post questions from the file until the file is mapped;
	while(parser.loadNext())
	{
		post(parser.next);
		if(userExit)
			return;
		if(userRedo)
			push(parser.next);		//Adds the query to the queue.
		orderIndex++;
		
		while(queryQueue.peek() != null && queryQueue.peek().getPosition == orderIndex)
		{
			post(queryQueue.peek());
			if(userExit)
				return;
			if(userRedo)
				push(queryQueue.poll());	//Pushes the query further down the queue.
			else
				queryQueue.poll();			//Removes the question from the queue.
			orderIndex++;
		}	//Any questions remaining in the queue are higher than orderIndex and should be asked later.
	}	//File is now loaded.
	
	
	//post random questions from the file, forever
  }
}
