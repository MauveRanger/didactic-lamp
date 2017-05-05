import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;

//Note: This is NOT a valid XML parser. It does not validate, it doesn't follow dtds,
//and it intentionally ignores certain exceptional conditions.
//Error output is on System.err. It includes problems that may affect operation of this
//parser, but not everything that a validating parser would catch.
//Use at your own risk! Or if you're lazy. Like this parser.

public class ImproperXMIterator
{
	String fileName;
	Reader input;
	int currentIndex;
	int currentChar;
	//A running total of the lines based on how the document would appear in a text editor.
	//Counts '\n' characters, which is good enough unless something is very wrong.
	//All instances of input.read() are followed by a check if a newline character was read.
	int lineCount = 0;
	StringBuilder contentsBuilder = new StringBuilder();
	
	public ImproperXMIterator(String fileName) throws FileNotFoundException
	{
		//System.out.println("ImproperXMIterator(" + fileName + "). lastChar: " + (char) currentChar + " currentIndex: " + currentIndex);
		this.fileName = fileName;
		input = new BufferedReader(new FileReader(fileName));
		currentIndex = -1;
		read();
	}
	
	//Returns a QuizElement representing the first instance of that node name.
	//Returns null if such node does not exist.
	public ImproperXMElement next()
	{
		//System.out.println("next(). lastChar: " + (char) currentChar + " currentIndex: " + currentIndex);
		if(currentChar == -1)			//Read at least once in case we're parsing a log file being appended to or something.
			read();
		while(Character.isWhitespace(currentChar))
			read();
		if(currentChar == -1)			//End of file
			return null;
		ImproperXMElement josh = new ImproperXMElement();
		if(currentChar == '<')			//Start of tag.
		{
			josh.start = currentIndex;
			josh.isData = false;
			josh.contents = loadTag();
			josh.end = currentIndex;
			josh.selfClosing = josh.contents.charAt(josh.contents.length() - 1) == '/';
			josh.closing = !josh.selfClosing && josh.contents.charAt(0) == '/';
			josh.isElement = !(josh.contents.charAt(0) == '!' || josh.contents.charAt(0) == '?');
		} else {						//Start of element.
			josh.start = currentIndex;
			josh.isData = true;
			josh.contents = loadElement();
			josh.end = currentIndex;
			josh.selfClosing = false;
			josh.closing = false;
			josh.isElement = false;
		}
		return josh;
	}
	
	
	//Reads characters until an instance of '>' is reached. Includes the '<' and the '>' if present.
	//Returns the characters read. "" if nothing found or eof reached before parsing anything.
	//Whitespace characters are compressed to single ' ' spaces unless in quotes.
	//Spaces are dropped if they follow '<' or '/' outside of quotes.
	//Can handle '\'' (single quotes) and '"' (double quotes).
	String loadTag()
	{
		//System.out.println("loadTag(). lastChar: " + (char) currentChar + " currentIndex: " + currentIndex);
		contentsBuilder.setLength(0);			//Reuse the same StringBuilder; I've heard it's faster.
		boolean singleQuotes = false;		//Keeps track while parsing the string.
		boolean doubleQuotes = false;
		while(currentChar != '>' && currentChar != -1)
		{
			//Append the character. Special cases for whitespace, quotes, and '<' and '/'.
			if(Character.isWhitespace(currentChar))
				if(singleQuotes || doubleQuotes)
				{	//Preserve whitespace for attribute literals.
					contentsBuilder.append((char) currentChar);
					read();
				} else {
					//Replace unquoted whitespace with a single space.
					contentsBuilder.append(' ');
					skipBlanks();
				}
			else
			{	//Append all non-whitespace characters, no questions asked.
				contentsBuilder.append((char) currentChar);
				//Compress whitespace for nonliterals after < and /.
				if((currentChar == '<' || currentChar == '/') && !(singleQuotes || doubleQuotes))
				{
					read();
					skipBlanks();		//Skip all spaces after '<' and '/' (Space here would normally invalidate the document.).
				}
				else
				{
					if(currentChar == '"')
						if(!singleQuotes)	//Double quotes are not control characters inside of single quotes.
							doubleQuotes = !doubleQuotes;
					if(currentChar == '\'')
						if(!doubleQuotes)	//Single quotes are not controls inside double quotes.
							singleQuotes = !singleQuotes;
					read();
				}
			}	//currentChar has been appended, the reader has been pushed forwards, and chars have been read as necessary.
		}	//End of file or '>' has been reached.

		if(currentChar == -1)
			System.err.print("Error parsing line " + lineCount + ": Unexpected end of file in tag.");
		else
		{
			contentsBuilder.append((char) currentChar);	//Should always be '>'. I think.
			read();			//Move on past the element. All methods should end with currentChar sitting one char beyond themselves.
		}
		
		return contentsBuilder.toString();
	}
	
	
	//Reads characters until another tag begins.
	//Returns the characters read
	//Whitespace characters other than SPACE_SEPARATOR (' ') are dropped. Doubled spaces are dropped.
	//Note that, because loadElement() is called after next() has already skipped space until either '<' or something else,
	//we are excluding the preceding whitespace from this element. This is NOT valid xml parsing!
	//However, it makes the file look prettier when you're editing it.
	String loadElement()
	{
		//System.out.println("loadElement(). lastChar: " + (char) currentChar + " currentIndex: " + currentIndex);
		int trailingWhitespaceCount = 0;				//Exclude the final block of whitespace. This is also NOT valid xml!
		contentsBuilder.setLength(0);			//Reuse the same StringBuilder; I've heard it's faster.
		while(currentChar != '<' && currentChar != -1)
		{
			if(currentChar == '>')	//This isn't supposed to happen. Maybe a sign of malformed xml?
			{
				System.err.println("While parsing document " + fileName + ", there was an error at line " + lineCount + ":");
				System.err.println("\t'>' was encountered in the middle of an element.");
			}
			if(Character.isWhitespace(currentChar))
				trailingWhitespaceCount++;
			else
				trailingWhitespaceCount = 0;
			contentsBuilder.append((char) currentChar);
			read();
		}
		
		//We don't need to push currentChar further. We have either reached EOF or are sitting on an '<'
		//Contents will exclude all trailing whitespace.
		return contentsBuilder.substring(0, contentsBuilder.length() - trailingWhitespaceCount);
	}
	
	void skipBlanks()
	{
		while(Character.isWhitespace(currentChar))
			read();		
	}

	//Per standard best practice, swallows exceptions and use 'global' (instance) vars :P
	private void read()
	{
		try {
			currentChar = input.read();
			//This is a really terrible, brittle way of approximating how many bytes FileReader read.
			//It only works with ascii and UTF-8, and if FileReader does anything strange to the
			//data it'll break. I don't know why FR can't just give you the pointer...
			if(currentChar != -1)
				if(currentChar > 255)
					currentIndex += 2;	//Presumably, this data must have been stored in two bytes.
				else
					currentIndex++;
			if(currentChar == '\n')
				lineCount++;
		} catch(IOException e) {
			System.out.println("There was an exception reading the file " + fileName + ":");
			e.printStackTrace();
			currentChar = -1;
		}
	}
}
