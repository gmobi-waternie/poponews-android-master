package com.gmobi.poponews.model;

import java.util.List;

/**
 * Created by Administrator on 2/1 0001.
 */
public class EditionList {


	/**
	 * data : [{"icon":"b3e2e89b0afee2705eb4b1801e265bfa","country":"cn","lang":"cn","name":"中国（简体中文）","id":"test"},{"icon":"f742ece21a9cd18a2922c56572d891db","country":"in","lang":"en","name":"India (English)","id":"test.india.en"},{"icon":"b88ffc6def22662e06a7922384de4607","country":"tw","lang":"tw","name":"台灣（繁體中文）","id":"test.taiwan.tw"},{"icon":"d63d93e89fcae2d27efd8691d51c320f","country":"id","lang":"in","name":"Indonesia(Indonesian)","id":"test.indonesia"},{"icon":"5e0033fba7f733867e2251b842d38aee","country":"my","lang":"en","name":"Malaysia (English)","id":"test.my"},{"icon":"5e0033fba7f733867e2251b842d38aee","country":"my","lang":"my","name":"Malaysia (Malay)","id":"test.malaysia"},{"icon":"6ada429aea1c60b45bf830bb9beeb3ee","country":"ph","lang":"en","name":"Philippines(English)","id":"test.ph"},{"icon":"54e3dd213658201dae0a0e902f4340e3","country":"th","lang":"th","name":"Thailand(Thai)","id":"test.th"}]
	 * baseUrl : http://test.poponews.net/files/
	 */

	private String baseUrl;
	/**
	 * icon : b3e2e89b0afee2705eb4b1801e265bfa
	 * country : cn
	 * lang : cn
	 * name : 中国（简体中文）
	 * id : test
	 */

	private List<EditionEntity> data;

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void setData(List<EditionEntity> data) {
		this.data = data;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public List<EditionEntity> getData() {
		return data;
	}

	public static class EditionEntity {
		private String icon;
		private String country;
		private String lang;
		private String name;
		private String id;

		public void setIcon(String icon) {
			this.icon = icon;
		}

		public void setCountry(String country) {
			this.country = country;
		}

		public void setLang(String lang) {
			this.lang = lang;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getIcon() {
			return icon;
		}

		public String getCountry() {
			return country;
		}

		public String getLang() {
			return lang;
		}

		public String getName() {
			return name;
		}

		public String getId() {
			return id;
		}
	}
}
