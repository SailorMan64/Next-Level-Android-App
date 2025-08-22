package jp.redmine.redmineclient.form.helper;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.redmine.redmineclient.activity.handler.WebviewActionInterface;
import jp.redmine.redmineclient.entity.RedmineConnection;
import jp.redmine.redmineclient.model.ConnectionModel;

public class WebViewHelper {
	private WebviewActionInterface action;
	private final RedmineConvertToHtmlHelper converter = new RedmineConvertToHtmlHelper();
	private final Pattern patternIntent = Pattern.compile(RedmineConvertToHtmlHelper.URL_PREFIX);

	// Convert common image syntaxes to <img> if needed
	private static final Pattern IMG_MARKDOWN =
			Pattern.compile("!\\[[^\\]]*\\]\\((https?://[^\\s)]+)\\)");
	private static final Pattern IMG_TEXTILE =
			Pattern.compile("!([^\\s!]+\\.(?:png|jpe?g|gif|webp))(\\?[^\\s!]*)?!");
	private static final Pattern IMG_BARE =
			Pattern.compile("(?i)(https?://[^\\s\"'<>]+\\.(?:png|jpe?g|gif|webp))(\\?[^\\s\"'<>]+)?");
	private static final Pattern IMG_TAG =
			Pattern.compile("(?is)<img\\s+[^>]*src\\s*=\\s*([\"'])(.*?)\\1");

	public void setup(WebView view){
		setupWebView(view);
		setupHandler(view);
	}

	protected void setupWebView(WebView view){
		WebSettings s = view.getSettings();
		s.setBlockNetworkLoads(false);          // allow network
		s.setLoadsImagesAutomatically(true);    // load images
		s.setDomStorageEnabled(true);
		if (Build.VERSION.SDK_INT >= 21) {
			s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
		}
		// Accept cookies (useful if you later set a Redmine session cookie)
		CookieManager.getInstance().setAcceptCookie(true);
		if (Build.VERSION.SDK_INT >= 21) {
			CookieManager.getInstance().setAcceptThirdPartyCookies(view, true);
		}
	}

	public void setAction(WebviewActionInterface act){ action = act; }

	protected void setupHandler(WebView view){
		view.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Matcher m = patternIntent.matcher(url);
				if(m.find()){
					return RedmineConvertToHtmlHelper.kickAction(action, m.replaceAll(""));
				} else if (action != null) {
					return action.url(url, null);
				} else {
					return super.shouldOverrideUrlLoading(view, url);
				}
			}
		});
	}

	public void setContent(WebView view, WikiType type, final int connectionid, final long project, final String text){
		String inner = converter.parse(text, type, connectionid, project).replace("\" width=\"500\">","");
		Log.d("TAGdsasdaD", inner);

		inner = unescapeIfEscapedHtml(inner);   // in case converter escaped tags
		inner = injectImageTags(inner);         // convert bare links/markdown/textile to <img>

		// Resolve base and API key
		String baseUrl = resolveBaseUrl(view.getContext(), connectionid);
		String apiKey  = resolveApiKey(view.getContext(), connectionid);

		// Add ?key=API_KEY to <img src> that are relative or on the same host
		if (!TextUtils.isEmpty(apiKey)) {
			inner = addApiKeyToImageSrc(inner, baseUrl, apiKey);
		}

		view.loadDataWithBaseURL(
				baseUrl, HtmlHelper.getHtml(view.getContext(), inner, ""),
				"text/html", "UTF-8", null
		);
	}

	public void setContent(WebView view, String text, WikiType type){
		String inner = converter.parse(text, type);

		Log.d("TAGdsasdaD", inner);

		inner = unescapeIfEscapedHtml(inner);
		inner = injectImageTags(inner);
		// No connection context here; absolute images only
		view.loadDataWithBaseURL("about:blank",
				HtmlHelper.getHtml(view.getContext(), inner, ""),
				"text/html","UTF-8", null);
	}

	// ---------------- helpers ----------------

	private String resolveBaseUrl(Context ctx, int connectionId){
		try {
			RedmineConnection con = ConnectionModel.getItem(ctx, connectionId);
			if (con != null && con.getUrl() != null) {
				String base = con.getUrl().trim();
				return base.endsWith("/") ? base : (base + "/");
			}
		} catch (Exception ignore) {}
		return "about:blank";
	}

	private String resolveApiKey(Context ctx, int connectionId){
		try {
			RedmineConnection con = ConnectionModel.getItem(ctx, connectionId);
			if (con != null) {
				// adapt to your model: token may be named getApiKey(), getToken(), etc.
				return con.getToken();
			}
		} catch (Exception ignore) {}
		return null;
	}

	private String unescapeIfEscapedHtml(String s){
		if (s == null || s.isEmpty()) return "";
		if (s.contains("&lt;")) {
			s = s.replace("&lt;", "<")
				//	.replace("&gt;", ">")
				//	.replace("&quot;", "\"")
					.replace("&#39;", "'")
					.replace("&amp;", "&");
		}
		return s;
	}

	private String injectImageTags(String in){
		if (TextUtils.isEmpty(in)) return "";
		String s = in;
		s = IMG_MARKDOWN.matcher(s)
				.replaceAll("<img src=\"$1\" style=\"max-width:100%;height:auto;\" />");
		s = IMG_TEXTILE.matcher(s)
				.replaceAll("<img src=\"$1$2\" style=\"max-width:100%;height:auto;\" />");
		s = IMG_BARE.matcher(s)
				.replaceAll("<img src=\"$1$2\" style=\"max-width:100%;height:auto;\" />");
		return s;
	}

	private String addApiKeyToImageSrc(String html, String baseUrl, String apiKey){
		if (TextUtils.isEmpty(html) || TextUtils.isEmpty(apiKey)) return html;
		String host = hostOf(baseUrl);

		StringBuffer out = new StringBuffer(html.length() + 64);
		Matcher m = IMG_TAG.matcher(html);
		while (m.find()) {
			String quote = m.group(1);
			String src   = m.group(2);

			String newSrc = src;
			boolean sameHostOrRelative = isRelative(src) || TextUtils.equals(host, hostOf(src));

			if (sameHostOrRelative && !src.contains("key=")) {
				if (src.contains("?")) newSrc = src + "&key=" + apiKey;
				else                    newSrc = src + "?key=" + apiKey;
			}
			// rebuild the <img ... src="...">
			String repl = m.group(0).replace(quote + src + quote, quote + newSrc + quote);
			m.appendReplacement(out, Matcher.quoteReplacement(repl));
		}
		m.appendTail(out);
		return out.toString();
	}

	private static boolean isRelative(String url) {
		return url != null && (url.startsWith("/") || !url.startsWith("http"));
	}

	private static String hostOf(String url){
		try {
			if (TextUtils.isEmpty(url)) return "";
			URI u = URI.create(url);
			return u.getHost() == null ? "" : u.getHost();
		} catch (Exception e) {
			return "";
		}
	}
}
