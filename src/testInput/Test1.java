package testInput;
public class Test1
{
	public void m()
	{
		History.addHistoryListener(this);
		String token = History.getToken();
		if (token.length() == 0)
		{
			History.newItem(INIT_STATE);
		}
		else
		{
			History.findCurrentHistoryState();
		}
	}
}