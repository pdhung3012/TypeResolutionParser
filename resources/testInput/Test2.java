package testInput;

import com.google.android.maps.MapView;
import gen.R;

public class Test2 extends Activity 
{
	@Override
	public void onCreate( Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.setWebViewClient(new MyWebViewClient(this));
		webview.loadUrl("...");
		Super a;
		org.junit.tests.anotherpackage.Super b = "abc";
		a = b;
		Char c;
		String d = "abc";
		c = d.getIndexOf(0);
		MapView myMapView = (MapView) findViewById(R.id.lay);
	}
}