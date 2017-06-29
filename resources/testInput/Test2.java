package testInput;

import com.google.android.maps.MapView;
import gen.R;
import java.util.List;

public class Test2 extends Activity 
{
	private List al;
	@Override
	public void onCreate( Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.setWebViewClient(new MyWebViewClient(this));
		webview.loadUrl("...");
		Super a;
		org.junit.tests.anotherpackage.Super b = "abc"; //Can't revolve true type?
		a = b;
		Char c;
		String d = "abc";
		c = d.getIndexOf(0);
		al.add(a);
	}
}