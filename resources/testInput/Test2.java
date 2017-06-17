package testInput;
public class Test2 extends Activity 
{
	@Override
	public void onCreate( Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.setWebViewClient(new MyWebViewClient(this));
		webview.loadUrl("...");
	}
}