public class ImproperXMElement
{
	public boolean isData;						//Comments and processing instructions are not data. All else are.
	public boolean isElement;					//Tags, comments, and instructions are not elements. All else are.
	public boolean closing, selfClosing;		//Closing tags start with </. selfClosing end with />. A tag cannot be both.
	public int start, end;						//Tag data extends from [start, end).
	//											  Do not use to calculate contents.length(); contents is stripped of whitespace.
	
	public String contents;						//Character data. Is either the element itself or the attributes.
	
	public ImproperXMElement(){}
	
	public ImproperXMElement(boolean isData, boolean isElement, boolean closing, boolean selfClosing, int start, int end)
	{	
		this.isData = isData;
		this.isElement = isElement;
		this.closing = closing;
		this.selfClosing = selfClosing;
		this.start = start;
		this.end = end;
	}
}
