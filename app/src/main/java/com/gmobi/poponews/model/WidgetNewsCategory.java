package com.gmobi.poponews.model;

import java.util.List;

/**
 * Created by Administrator on 12/21 0021.
 */
public class WidgetNewsCategory {


	public static final String CATEOGRY_PREFIX = "categories";


	/**
	 * categories : [{"name":"Social","id":"55b9845446c3f58420f7f14a","layout":"list","type":"social","extra":{"sources":[{"name":"baidu","title":"百度新闻","config":{"channels":[{"name":"国内","rss_url":"http://news.baidu.com/n?cmd=1&class=civilnews&tn=rss"},{"name":"国际","rss_url":"http://news.baidu.com/n?cmd=1&class=internews&tn=rss"},{"name":"军事","rss_url":"http://news.baidu.com/n?cmd=1&class=mil&tn=rss"},{"name":"财经","rss_url":"http://news.baidu.com/n?cmd=1&class=finannews&tn=rss"},{"name":"互联网","rss_url":"http://news.baidu.com/n?cmd=1&class=internet&tn=rss"},{"name":"房产","rss_url":"http://news.baidu.com/n?cmd=1&class=housenews&tn=rss"},{"name":"汽车","rss_url":"http://news.baidu.com/n?cmd=1&class=autonews&tn=rss"},{"name":"体育","rss_url":"http://news.baidu.com/n?cmd=1&class=sportnews&tn=rss"},{"name":"娱乐","rss_url":"http://news.baidu.com/n?cmd=1&class=enternews&tn=rss"},{"name":"游戏","rss_url":"http://news.baidu.com/n?cmd=1&class=gamenews&tn=rss"},{"name":"教育","rss_url":"http://news.baidu.com/n?cmd=1&class=edunews&tn=rss"},{"name":"女人","rss_url":"http://news.baidu.com/n?cmd=1&class=healthnews&tn=rss"},{"name":"科技","rss_url":"http://news.baidu.com/n?cmd=1&class=technnews&tn=rss"},{"name":"社会","rss_url":"http://news.baidu.com/n?cmd=1&class=socianews&tn=rss"}]}}]}}]
	 */

	private List<CategoriesEntity> categories;

	public void setCategories(List<CategoriesEntity> categories) {
		this.categories = categories;
	}

	public List<CategoriesEntity> getCategories() {
		return categories;
	}

	public static class CategoriesEntity {
		/**
		 * name : Social
		 * id : 55b9845446c3f58420f7f14a
		 * layout : list
		 * type : social
		 * extra : {"sources":[{"name":"baidu","title":"百度新闻","config":{"channels":[{"name":"国内","rss_url":"http://news.baidu.com/n?cmd=1&class=civilnews&tn=rss"},{"name":"国际","rss_url":"http://news.baidu.com/n?cmd=1&class=internews&tn=rss"},{"name":"军事","rss_url":"http://news.baidu.com/n?cmd=1&class=mil&tn=rss"},{"name":"财经","rss_url":"http://news.baidu.com/n?cmd=1&class=finannews&tn=rss"},{"name":"互联网","rss_url":"http://news.baidu.com/n?cmd=1&class=internet&tn=rss"},{"name":"房产","rss_url":"http://news.baidu.com/n?cmd=1&class=housenews&tn=rss"},{"name":"汽车","rss_url":"http://news.baidu.com/n?cmd=1&class=autonews&tn=rss"},{"name":"体育","rss_url":"http://news.baidu.com/n?cmd=1&class=sportnews&tn=rss"},{"name":"娱乐","rss_url":"http://news.baidu.com/n?cmd=1&class=enternews&tn=rss"},{"name":"游戏","rss_url":"http://news.baidu.com/n?cmd=1&class=gamenews&tn=rss"},{"name":"教育","rss_url":"http://news.baidu.com/n?cmd=1&class=edunews&tn=rss"},{"name":"女人","rss_url":"http://news.baidu.com/n?cmd=1&class=healthnews&tn=rss"},{"name":"科技","rss_url":"http://news.baidu.com/n?cmd=1&class=technnews&tn=rss"},{"name":"社会","rss_url":"http://news.baidu.com/n?cmd=1&class=socianews&tn=rss"}]}}]}
		 */

		private String name;
		private String id;
		private String layout;
		private String type;
		private ExtraEntity extra;

		public void setName(String name) {
			this.name = name;
		}

		public void setId(String id) {
			this.id = id;
		}

		public void setLayout(String layout) {
			this.layout = layout;
		}

		public void setType(String type) {
			this.type = type;
		}

		public void setExtra(ExtraEntity extra) {
			this.extra = extra;
		}

		public String getName() {
			return name;
		}

		public String getId() {
			return id;
		}

		public String getLayout() {
			return layout;
		}

		public String getType() {
			return type;
		}

		public ExtraEntity getExtra() {
			return extra;
		}

		public static class ExtraEntity {
			/**
			 * sources : [{"name":"baidu","title":"百度新闻","config":{"channels":[{"name":"国内","rss_url":"http://news.baidu.com/n?cmd=1&class=civilnews&tn=rss"},{"name":"国际","rss_url":"http://news.baidu.com/n?cmd=1&class=internews&tn=rss"},{"name":"军事","rss_url":"http://news.baidu.com/n?cmd=1&class=mil&tn=rss"},{"name":"财经","rss_url":"http://news.baidu.com/n?cmd=1&class=finannews&tn=rss"},{"name":"互联网","rss_url":"http://news.baidu.com/n?cmd=1&class=internet&tn=rss"},{"name":"房产","rss_url":"http://news.baidu.com/n?cmd=1&class=housenews&tn=rss"},{"name":"汽车","rss_url":"http://news.baidu.com/n?cmd=1&class=autonews&tn=rss"},{"name":"体育","rss_url":"http://news.baidu.com/n?cmd=1&class=sportnews&tn=rss"},{"name":"娱乐","rss_url":"http://news.baidu.com/n?cmd=1&class=enternews&tn=rss"},{"name":"游戏","rss_url":"http://news.baidu.com/n?cmd=1&class=gamenews&tn=rss"},{"name":"教育","rss_url":"http://news.baidu.com/n?cmd=1&class=edunews&tn=rss"},{"name":"女人","rss_url":"http://news.baidu.com/n?cmd=1&class=healthnews&tn=rss"},{"name":"科技","rss_url":"http://news.baidu.com/n?cmd=1&class=technnews&tn=rss"},{"name":"社会","rss_url":"http://news.baidu.com/n?cmd=1&class=socianews&tn=rss"}]}}]
			 */

			private List<SourcesEntity> sources;

			public void setSources(List<SourcesEntity> sources) {
				this.sources = sources;
			}

			public List<SourcesEntity> getSources() {
				return sources;
			}

			public static class SourcesEntity {
				/**
				 * name : baidu
				 * title : 百度新闻
				 * config : {"channels":[{"name":"国内","rss_url":"http://news.baidu.com/n?cmd=1&class=civilnews&tn=rss"},{"name":"国际","rss_url":"http://news.baidu.com/n?cmd=1&class=internews&tn=rss"},{"name":"军事","rss_url":"http://news.baidu.com/n?cmd=1&class=mil&tn=rss"},{"name":"财经","rss_url":"http://news.baidu.com/n?cmd=1&class=finannews&tn=rss"},{"name":"互联网","rss_url":"http://news.baidu.com/n?cmd=1&class=internet&tn=rss"},{"name":"房产","rss_url":"http://news.baidu.com/n?cmd=1&class=housenews&tn=rss"},{"name":"汽车","rss_url":"http://news.baidu.com/n?cmd=1&class=autonews&tn=rss"},{"name":"体育","rss_url":"http://news.baidu.com/n?cmd=1&class=sportnews&tn=rss"},{"name":"娱乐","rss_url":"http://news.baidu.com/n?cmd=1&class=enternews&tn=rss"},{"name":"游戏","rss_url":"http://news.baidu.com/n?cmd=1&class=gamenews&tn=rss"},{"name":"教育","rss_url":"http://news.baidu.com/n?cmd=1&class=edunews&tn=rss"},{"name":"女人","rss_url":"http://news.baidu.com/n?cmd=1&class=healthnews&tn=rss"},{"name":"科技","rss_url":"http://news.baidu.com/n?cmd=1&class=technnews&tn=rss"},{"name":"社会","rss_url":"http://news.baidu.com/n?cmd=1&class=socianews&tn=rss"}]}
				 */

				private String name;
				private String title;
				private ConfigEntity config;

				public void setName(String name) {
					this.name = name;
				}

				public void setTitle(String title) {
					this.title = title;
				}

				public void setConfig(ConfigEntity config) {
					this.config = config;
				}

				public String getName() {
					return name;
				}

				public String getTitle() {
					return title;
				}

				public ConfigEntity getConfig() {
					return config;
				}

				public static class ConfigEntity {
					/**
					 * channels : [{"name":"国内","rss_url":"http://news.baidu.com/n?cmd=1&class=civilnews&tn=rss"},{"name":"国际","rss_url":"http://news.baidu.com/n?cmd=1&class=internews&tn=rss"},{"name":"军事","rss_url":"http://news.baidu.com/n?cmd=1&class=mil&tn=rss"},{"name":"财经","rss_url":"http://news.baidu.com/n?cmd=1&class=finannews&tn=rss"},{"name":"互联网","rss_url":"http://news.baidu.com/n?cmd=1&class=internet&tn=rss"},{"name":"房产","rss_url":"http://news.baidu.com/n?cmd=1&class=housenews&tn=rss"},{"name":"汽车","rss_url":"http://news.baidu.com/n?cmd=1&class=autonews&tn=rss"},{"name":"体育","rss_url":"http://news.baidu.com/n?cmd=1&class=sportnews&tn=rss"},{"name":"娱乐","rss_url":"http://news.baidu.com/n?cmd=1&class=enternews&tn=rss"},{"name":"游戏","rss_url":"http://news.baidu.com/n?cmd=1&class=gamenews&tn=rss"},{"name":"教育","rss_url":"http://news.baidu.com/n?cmd=1&class=edunews&tn=rss"},{"name":"女人","rss_url":"http://news.baidu.com/n?cmd=1&class=healthnews&tn=rss"},{"name":"科技","rss_url":"http://news.baidu.com/n?cmd=1&class=technnews&tn=rss"},{"name":"社会","rss_url":"http://news.baidu.com/n?cmd=1&class=socianews&tn=rss"}]
					 */

					private List<ChannelsEntity> channels;

					public void setChannels(List<ChannelsEntity> channels) {
						this.channels = channels;
					}

					public List<ChannelsEntity> getChannels() {
						return channels;
					}

					public static class ChannelsEntity {
						/**
						 * name : 国内
						 * rss_url : http://news.baidu.com/n?cmd=1&class=civilnews&tn=rss
						 */

						private String name;
						private String rss_url;
						private List<String> rss_urls;

						public void setName(String name) {
							this.name = name;
						}

						public void setRss_url(String rss_url) {
							this.rss_url = rss_url;
						}

						public String getName() {
							return name;
						}

						public String getRss_url() {
							return rss_url;
						}

						public List<String> getRss_urls() {
							return rss_urls;
						}
						public void setRss_urls(List<String> rss_urls) {
							this.rss_urls = rss_urls;
						}
					}
				}
			}
		}
	}
}
