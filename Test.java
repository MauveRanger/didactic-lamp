import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Test
{
	public static void main(String[] args) throws Exception
	{
		BufferedReader sourceFile = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get("Test.txt"))));
		QueryLayer topLayer = new QueryLayer(sourceFile);
		sourceFile.close();
		Scanner keyboard = new Scanner(System.in);
		if(topLayer.pickChild() == null)
		{
			System.out.println("No data found.");
			return;
		} else {
			System.out.print("Data loaded. Proceed? y/n: ");
		}
		String response = keyboard.nextLine().toLowerCase();
		if(!response.equals("y"))
			return;
		System.out.println("To quit, enter q.");
		System.out.println("To proceed, press enter.");
		QueryLayer currentLayer = topLayer;
		do {
			currentLayer = currentLayer.pickChild();
			if(currentLayer == null)
				currentLayer = topLayer.pickChild();
			List<String> lines = currentLayer.getContent();
			int lastLine = lines.size() - 1;
			for(int i = 0; i < lastLine; i++)
				System.out.println(lines.get(i));
			System.out.print(lines.get(lastLine));
			response = keyboard.nextLine().toLowerCase();
		} while(!response.equals("q"));
	}
	
	protected static class QueryLayer
	{
		QueryLayer parent;
		List<String> content = new ArrayList<String>();
		List<QueryLayer> children = new ArrayList<QueryLayer>();
		
		public QueryLayer(BufferedReader sourceFile) throws Exception
		{
			String thisLine = sourceFile.readLine();
			QueryLayer currentLayer = this;
			int currentTier = 0;
			//So there's a couple cases
			//First, if the nextLine is on this height, then we just append it. Currentlayer.addContent(thisLine).
			//Second, if the nextLine is deeper, then we need to start adding layers until we are at the right layer.
			//	Then, we append it.
			//Third, if the nextLine is shallower, then we need to start dropping lines until we reach the line above
			//	where nextLine should go.
			while(thisLine != null)
			{
				int nextTier = tierOf(thisLine);
				if(nextTier != thisLine.length())
				{
					if(nextTier != currentTier)
						if(nextTier > currentTier)
						{
							do {
								currentLayer = new QueryLayer(currentLayer);
								currentTier++;
							} while(nextTier > currentTier);
						} else {
							do {
								currentLayer = currentLayer.getParent();
								currentTier--;
							} while(nextTier <= currentTier);
							currentLayer = new QueryLayer(currentLayer);
							currentTier++;
						}
					
					currentLayer.addContent(thisLine);
				}
				thisLine = sourceFile.readLine();
			}
		}
		
		protected QueryLayer(QueryLayer parent)
		{
			this.parent = parent;
			parent.addChild(this);
		}
		
		protected void printTree(int currentTier)
		{
			for(String contentItem : content)
				System.out.println(currentTier + ":" + contentItem);
			currentTier++;
			for(QueryLayer child : children)
				child.printTree(currentTier);
		}
		
		private void addChild(QueryLayer newKid)
		{
			children.add(newKid);
		}
		
		public void addContent(String addMe)
		{
			content.add(addMe.trim());
		}
		
		public QueryLayer swapParent(QueryLayer newParent)
		{
			QueryLayer temp = parent;
			parent = newParent;
			return temp;
		}
		
		public static int tierOf(String countMe)
		{
			int tierOf = 0;
			while(tierOf < countMe.length() && countMe.charAt(tierOf) == '\t')
				tierOf++;
			return tierOf + 1;
		}
		
		public QueryLayer getParent()
		{
			return parent;
		}
		
		public QueryLayer pickChild()
		{
			if(children.size() > 0)
				return children.get((int) (Math.random() * children.size()));
			else
				return null;
		}
		
		public List<String> getContent()
		{
			return content;
		}
	}
}