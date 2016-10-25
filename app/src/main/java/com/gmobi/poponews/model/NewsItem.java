package com.gmobi.poponews.model;


import com.gmobi.poponews.service.IConfigService;
import com.gmobi.poponews.util.DipHelper;
import com.momock.app.App;
import com.momock.data.DataList;
import com.momock.data.DataListView;
import com.momock.data.DataMap;
import com.momock.util.Convert;
import com.momock.util.SystemHelper;


public class NewsItem extends DataMap<String, Object> {
	public static final String NEWS_TYPE_REGULAR = "article";
	public static final String NEWS_TYPE_IMAGE = "photo";
	public static final String NEWS_TYPE_AD = "ad";
	public static final String NEWS_TYPE_FACEBOOKAD = "facebookad";
	public static final String NEWS_TYPE_FEATURED = "featured";


	public static final String NEWS_LAYOUT_LARGEPIC = "LARGE_PIC";
	public static final String NEWS_LAYOUT_MULTIPICS = "MULTI_PICS";
	public static final String NEWS_LAYOUT_TITLEONLY = "TITLE_ONLY";
	public static final String NEWS_LAYOUT_NORMAL_RIGHT = "NORMAL_RIGHT_PIC";
	public static final String NEWS_LAYOUT_AD = "AD";
	public static final String NEWS_LAYOUT_FACEBOOKAD = "FACEBOOKAD";


	public static final String NEWS_IMG_LIMIT = ".220x167t5";


	public static final int NEWS_FAV_NONE = 0;
	public static final int NEWS_FAV_LIST = 1;

	public static final int NEWS_NOT_READ = 0;
	public static final int NEWS_HAVE_READ = 1;

	public static final int NEWS_NOT_FEATURED = 0;
	public static final int NEWS_FEATURED = 1;
	
	/*
   
    {"_id":"54ee77a52358c6c97d69e24a",
    "_cid":"图片_test","releaseTime":1424914338247,
    "title":"以色列摄影师拍摄冰岛冰与火的奇幻世界",
    "mm":[
    {"t":1,"f":"8c24ee823b116d722db1868cbccb0f80","w":1200,"h":800,"desc":"2015年2月25日据外媒报道，来自以色列的34岁摄影师Erez Marom在自去年8月开始喷发的冰岛Holuhaun火山附近拍摄到了一组令人难以置信的图片。高达1000摄氏度的熔岩从火山口喷涌而出，蔓延27平方英里（约70平方公里），与四周的冰川形成了鲜明对比，冰川与火山谱写下“冰与火之歌”。"},
    {"t":1,"f":"18ae3121f9a241589c6d865908619776","w":1190,"h":800,"desc":"大量的鲱鱼被冻在峡湾的冰面之下。"},
    {"t":1,"f":"abac320d599a8e3cf970ccb2cc566c3f","w":1200,"h":800,"desc":"壮美的冰川。"},
    {"t":1,"f":"03b8dc0e9037528b1df4b5d93e975462","w":1200,"h":785,"desc":"壮美的冰川。"},
    {"t":1,"f":"dd658a0892e20f212b0f43b3be97cd77","w":1200,"h":800,"desc":"正在喷发的火山。"},
    {"t":1,"f":"04f67541b78b6b014e41e43d4ad6685e","w":1200,"h":800,"desc":"火山熔岩蔓延。"}],
    "preview":"8c24ee823b116d722db1868cbccb0f80",
    "p_domain":"cankaoxiaoxi.com",
    "source":"http://m.cankaoxiaoxi.com/photo/20150226/679692.shtml",
    "body":"161ee0eb73257b01288b8815092bae93",
    "type":"photo","p_name":"参考消息","p_icon":"0fefb117a2d7b69e0b10d725a6b0b8dd"}
    *
    */


	public static final String Id = "_id";
	public static final String CategoryId = "_cid";

	public static final String ReleaseTime = "releaseTime";
	public static final String Summary = "summary";
	public static final String Title = "title";


	public static final String Content = "content";
	public static final String PublisherDomain = "p_domain";
	public static final String PublisherName = "p_name";
	public static final String PublisherIcon = "p_icon";
	public static final String Source = "source";
	public static final String Body = "body";

	public static final String Type = "type";
	public static final String Preview = "preview";

	public static final String Template = "template";


	public static final String Fav = "fav";
	public static final String MyFav = "myfav";
	public static final String HaveRead = "haveread";
	public static final String Favnum = "favnum";
	public static final String Emo = "Emoji";
	public static final String Imgs = "Imgs";
	public static final String Icon = "icon";
	public static final String Status = "status";
	public static final String AdObj = "adobj";


	public static final String Preview1 = "preview1";
	public static final String Preview2 = "preview2";
	public static final String Preview3 = "preview3";
	public static final String GridPreview = "gridpreview";

	public static final String Grid1Preview = "grid1preview";
	public static final String Grid2Preview = "grid2preview";
	public static final String Grid3Preview = "grid3preview";

	public static final String Pin2Preview = "pin2preview";
	public static final String PinPreview = "pinpreview";
	public static final String ListPreview = "listpreview";
	public static final String FeaturePreview = "featurepreview";

	public static final String Go2Src = "go2source";

	public static final String AdInflated = "adInflated";

	private static final String CommentCount = "comment_count";

	public static final String Featured = "featured";

	public static final String Comment = "comment";
	private DataListView<CommentEntity> commentView;

	public static final String LayoutType = "layout_type";
	public static final String FirstPage = "first_page";


	public String get_id() {
		return (String) this.getProperty(Id);
	}

	public void set_id(String id) {
		this.setProperty(Id, id);
	}


	public String get_cid() {
		return (String) this.getProperty(CategoryId);
	}

	public void set_cid(String id) {
		this.setProperty(CategoryId, id);
	}

	public int getCommentCount() {
		return Convert.toInteger(this.getProperty(CommentCount));
	}

	public void setCommentCount(int commentCount) {
		this.setProperty(CommentCount, commentCount);
	}

	public long getReleaseTime() {
		return (Long) this.getProperty(ReleaseTime);
	}

	public void setReleaseTime(long time) {
		this.setProperty(ReleaseTime, time);
	}


	public String getSummary() {
		return (String) this.getProperty(Summary);
	}

	public void setSummary(String summary) {
		this.setProperty(Summary, summary);
	}

	public String getTitle() {
		return (String) this.getProperty(Title);
	}

	public void setTitle(String title) {
		this.setProperty(Title, title);
	}

	public String getPdomain() {
		return Convert.toString(this.getProperty(PublisherDomain));
	}

	public void setPdomain(String domain) {
		this.setProperty(PublisherDomain, domain);
	}

	public String getPicon() {
		return Convert.toString(this.getProperty(PublisherIcon));
	}

	public void setPicon(String icon) {
		this.setProperty(PublisherIcon, icon);
	}

	public String getPname() {
		return (String) this.getProperty(PublisherName);
	}

	public void setPname(String name) {
		this.setProperty(PublisherName, name);
	}

	public String getSource() {
		return (String) this.getProperty(Source);
	}

	public void setSource(String source) {
		this.setProperty(Source, source);
	}

	public String getPreview() {
		return (String) this.getProperty(Preview);

	}

	public void setPreview(String preview) {
		this.setProperty(Preview, preview);
	}

	public String getFeaturePreview() {
		return (String) this.getProperty(FeaturePreview);

	}

	public void setFeaturePreview(String preview) {
		this.setProperty(FeaturePreview, preview);
	}


	public void setPinPreview(String preview) {
		this.setProperty(PinPreview, preview);
	}

	public String getPinPreview() {

		return (String) this.getProperty(PinPreview);
	}


	public String calcPinPreview() {
		DataList<NewsImage> imgs = (DataList<NewsImage>) this.getProperty(Imgs);
		int widthLimit = SystemHelper.getScreenWidth(App.get());
		int heightLimit = DipHelper.dip2px(240);
		int i;
		NewsImage found = null;
		for (i = 0; i < imgs.getItemCount(); i++) {
			NewsImage img = imgs.getItem(i);
			if (img.getWidth() > widthLimit && img.getHeight() > heightLimit) {
				found = img;
				break;
			}
		}
		if (found != null) {
			return found.getFile() + "." + widthLimit + "x" + heightLimit;
		}

		int big = 0;
		for (i = 0; i < imgs.getItemCount(); i++) {
			NewsImage img = imgs.getItem(i);
			if (img.getWidth() + img.getHeight() > big) {
				big = img.getWidth() + img.getHeight();
				found = img;
			}
		}

		return found.getFile() + "." + widthLimit + "x" + heightLimit;

	}

	public String getPin2Preview() {
		return (String) this.getProperty(Pin2Preview);

	}

	public void setPin2Preview(String preview) {
		this.setProperty(Pin2Preview, preview);
	}


	public String getListPreview() {
		return (String) this.getProperty(ListPreview);

	}

	public void setListPreview(String preview) {
		this.setProperty(ListPreview, preview);
	}


	public String getGridPreview() {
		return (String) this.getProperty(GridPreview);

	}

	public void setGridPreview(String preview) {
		this.setProperty(GridPreview, preview);
	}


	public String getGrid1Preview() {
		return (String) this.getProperty(Grid1Preview);

	}

	public void setGrid1Preview(String preview) {
		this.setProperty(Grid1Preview, preview);
	}


	public String getGrid2Preview() {
		return (String) this.getProperty(Grid2Preview);

	}

	public void setGrid2Preview(String preview) {
		this.setProperty(Grid2Preview, preview);
	}


	public String getGrid3Preview() {
		return (String) this.getProperty(Grid3Preview);

	}

	public void setGrid3Preview(String preview) {
		this.setProperty(Grid3Preview, preview);
	}


	public String getContent() {
		return (String) this.getProperty(Content);
	}

	public void setContent(String content) {
		this.setProperty(Content, content);
	}

	public String getType() {
		return (String) this.getProperty(Type);
	}

	public void setType(String type) {
		this.setProperty(Type, type);
	}
	
	/*
	public int getFeatured() {
		return  (Integer)this.getProperty(Featured);
	}
	
	public void setFeatured(int featured) {
		this.setProperty(Featured, featured);
	}*/

	public int getFav() {
		return (Integer) this.getProperty(Fav);
	}

	public void setFav(int fav) {
		this.setProperty(Fav, fav);
	}

	public int getMyFav() {
		return (Integer) this.getProperty(MyFav);
	}

	public void setMyFav(int fav) {
		this.setProperty(MyFav, fav);
	}

	public int getHaveRead() {
		return (Integer) this.getProperty(HaveRead);
	}

	public void setHaveRead(int read) {
		this.setProperty(HaveRead, read);
	}


	@SuppressWarnings("unchecked")
	public EmoVote getEmo() {
		return (EmoVote) this.getProperty(Emo);
	}

	public void setEmo(EmoVote emo) {
		this.setProperty(Emo, emo);
	}

	@SuppressWarnings("unchecked")
	public DataList<NewsImage> getImgs() {
		return (DataList<NewsImage>) this.getProperty(Imgs);
	}


	public void setImgs(DataList<NewsImage> imgs) {


		this.setProperty(Imgs, imgs);
		this.setProperty(Preview1, "");
		this.setProperty(Preview2, "");
		this.setProperty(Preview3, "");


		if (imgs == null)
			return;

		int count = imgs.getItemCount();
		IConfigService cs = App.get().getService(IConfigService.class);

		if (count >= 1) {
			this.setProperty(Preview1, cs.getBaseImageUrl() + imgs.getItem(0).getFile() + NEWS_IMG_LIMIT);
		}

		if (count >= 2) {
			this.setProperty(Preview2, cs.getBaseImageUrl() + imgs.getItem(1).getFile() + NEWS_IMG_LIMIT);
		}
		if (count >= 3) {
			this.setProperty(Preview3, cs.getBaseImageUrl() + imgs.getItem(2).getFile() + NEWS_IMG_LIMIT);
		}


	}

	public void updateImgs(String pre1, String pre2, String pre3) {
		IConfigService cs = App.get().getService(IConfigService.class);
		String baseUrl = cs.getBaseImageUrl();

		this.setProperty(Preview1, baseUrl + pre1 + NEWS_IMG_LIMIT);
		this.setProperty(Preview2, baseUrl + pre2 + NEWS_IMG_LIMIT);
		this.setProperty(Preview3, baseUrl + pre3 + NEWS_IMG_LIMIT);
	}

	public String getPreview1() {
		return (String) this.getProperty(Preview1);
	}

	public String getPreview2() {
		return (String) this.getProperty(Preview2);
	}

	public String getPreview3() {
		return (String) this.getProperty(Preview3);
	}


	public int getStatus() {
		return Integer.parseInt((String) this.getProperty(Status));
	}

	public void setStatus(int status) {
		this.setProperty(Status, status);
	}

	public String getBody() {
		return (String) this.getProperty(Body);
	}

	public void setBody(String body) {
		this.setProperty(Body, body);
	}

	public String getTemplate() {
		return (String) this.getProperty(Template);
	}

	public void setTemplate(String template) {
		this.setProperty(Template, template);
	}

	public void setAdObj(Object ad) {
		this.setProperty(AdObj, ad);
	}

	public Object getAdObj() {
		return this.getProperty(AdObj);
	}

	public boolean getAdInflated() {
		if (hasProperty(AdInflated))
			return Convert.toBoolean(this.getProperty(AdInflated));
		return false;
	}

	public void setAdInflated(boolean i) {
		this.setProperty(AdInflated, i);
	}


	public boolean getGo2Src() {
		if (hasProperty(Go2Src))
			return Convert.toBoolean(this.getProperty(Go2Src));
		return false;
	}

	public void setGo2Src(boolean g) {
		this.setProperty(Go2Src, g);
	}


	public void setCommentList(Object c) {
		this.setProperty(Comment, c);
	}

	public Object getCommentList() {
		return this.getProperty(Comment);
	}

	public DataListView<CommentEntity> getCommentView() {
		return commentView;
	}


	public void setCommentView(DataListView<CommentEntity> commentView) {
		this.commentView = commentView;
	}

	public String getLayoutType() {
		if (!hasProperty(LayoutType))
			return NEWS_LAYOUT_NORMAL_RIGHT;
		return (String) this.getProperty(LayoutType);
	}

	public void setLayoutType(String type) {
		this.setProperty(LayoutType, type);
	}

	public boolean isFirstPage() {
		if (hasProperty(FirstPage))
			return Convert.toBoolean(this.getProperty(FirstPage));
		return false;
	}

	public void setFirstPage(boolean f) {
		this.setProperty(FirstPage, f);
	}


}

