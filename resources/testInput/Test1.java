package testInput;
public class Test1
{
	public void m()
	{
		History.addHistoryListener(this);//2
		String token = History.getToken();//3
		if (token.length() == 0)//4
		{
			History.newItem(INIT_STATE);//5
		}
		else//6
		{
			History.findCurrentHistoryState();//7
		}
	}
}