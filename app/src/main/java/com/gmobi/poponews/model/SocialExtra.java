package com.gmobi.poponews.model;

import java.util.List;

/**
 * Created by Administrator on 7/30 0030.
 * 用于存放网络的extra选项，以后数据结构可能更改
 */
public class SocialExtra {

	public static final String SOCIAL_TYPE_FACEBOOK = "facebook";
	public static final String SOCIAL_TYPE_TWITTER = "twitter";
	public static final String SOCIAL_TYPE_GOOGLE = "google";
	public static final String SOCIAL_TYPE_WEIBO = "weibo";
	public static final String SOCIAL_TYPE_BAIDU= "baidu";
	public static final String SOCIAL_TYPE_POPONEWS= "poponews";



	/**
	 * sources : [{"name":"google","config":{"channels":[{"rss_url":"http://news.google.com/news?cf=all&hl=zh-CN&ned=cn&output=rss","name":"焦点新闻"}]}},{"name":"facebook","config":{}},{"name":"twitter","config":{}}]
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
		 * name : google
		 * config : {"channels":[{"rss_url":"http://news.google.com/news?cf=all&hl=zh-CN&ned=cn&output=rss","name":"焦点新闻"}]}
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
			 * channels : [{"rss_url":"http://news.google.com/news?cf=all&hl=zh-CN&ned=cn&output=rss","name":"焦点新闻"}]
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
				 * rss_url : http://news.google.com/news?cf=all&hl=zh-CN&ned=cn&output=rss
				 * name : 焦点新闻
				 */
				private String rss_url;
				private List<String> rss_urls;
				private String name;

				public void setRss_url(String rss_url) {
					this.rss_url = rss_url;
				}

				public void setName(String name) {
					this.name = name;
				}

				public String getRss_url() {
					return rss_url;
				}

				public String getName() {
					return name;
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
