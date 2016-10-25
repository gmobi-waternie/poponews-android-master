package com.gmobi.poponews.service;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.gmobi.poponews.util.MD5;
import com.momock.binder.IContainerBinder;
import com.momock.cache.BitmapCache;
import com.momock.event.Event;
import com.momock.event.EventArgs;
import com.momock.event.IEvent;
import com.momock.event.IEventHandler;
import com.momock.http.HttpSession;
import com.momock.service.ICacheService;
import com.momock.service.IHttpService;
import com.momock.service.IImageService;
import com.momock.service.IImageService.ImageEventArgs;

import com.momock.util.Convert;
import com.momock.util.ImageHelper;
import com.momock.util.Logger;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;



public class MyImageService implements IImageService {
	Map<String, IEvent<ImageEventArgs>> allImageHandlers;
	BitmapCache<String> bitmapCache;
	Map<String, HttpSession> sessions;
	@Inject
	IHttpService httpService;
	@Inject
	ICacheService cacheService;
	List<ImageViewRefreshHandler> imageViewHandlers;
	List<MyImageService.BinderRefreshHandler> binderHandlers;

	public MyImageService() {
		this(16777216L);
	}

	public MyImageService(long cacheSize) {
		this.allImageHandlers = new HashMap();
		this.sessions = new HashMap();
		this.imageViewHandlers = new ArrayList();
		this.binderHandlers = new ArrayList();
		this.bitmapCache = new BitmapCache(cacheSize);
	}

	public MyImageService(long cacheSize, IHttpService httpService, ICacheService cacheService) {
		this.allImageHandlers = new HashMap();
		this.sessions = new HashMap();
		this.imageViewHandlers = new ArrayList();
		this.binderHandlers = new ArrayList();
		this.bitmapCache = new BitmapCache(cacheSize);
		this.httpService = httpService;
		this.cacheService = cacheService;
	}

	public Bitmap getBitmap(String fullUri) {
		Bitmap bmp = this.bitmapCache.get(fullUri);
		if(bmp != null && bmp.isRecycled()) {
			Logger.warn("Recycled Image : " + fullUri);
			this.bitmapCache.remove(fullUri);
			bmp = null;
		}

		return bmp;
	}

	public void addImageEventHandler(String fullUri, IEventHandler<ImageEventArgs> handler) {
		Object evt;
		if(this.allImageHandlers.containsKey(fullUri)) {
			evt = (IEvent)this.allImageHandlers.get(fullUri);
		} else {
			evt = new Event();
			this.allImageHandlers.put(fullUri, (IEvent<ImageEventArgs>) evt);
		}

		((IEvent)evt).addEventHandler(handler);
	}

	public void removeImageEventHandler(String fullUri, IEventHandler<ImageEventArgs> handler) {
		if(this.allImageHandlers.containsKey(fullUri)) {
			IEvent evt = (IEvent)this.allImageHandlers.get(fullUri);
			evt.removeEventHandler(handler);
		}

	}

	public File getCacheOf(String fullUri) {
		Logger.check(this.cacheService != null, "The cacheService must not be null!");
		return this.cacheService.getCacheOf(this.getClass().getName(), fullUri);
	}

	public void clearCache() {
		Logger.debug("Clear cache in ImageService!");
		this.bitmapCache.clear();
		System.gc();
	}

	public Bitmap loadBitmap(String fullUri) {
		try {
			try {
				return this.load(fullUri);
			} catch (OutOfMemoryError var3) {
				Logger.error(var3);
				this.clearCache();
				return this.load(fullUri);
			}
		} catch (Throwable var4) {
			Logger.error(var4);
			return null;
		}
	}



	protected Bitmap load(final String fullUri) {
		if(fullUri == null) {
			return null;
		} else {
			String uri = fullUri;
			int pos = fullUri.lastIndexOf(35);
			final int expectedWidth;
			final int expectedHeight;
			if(pos > 0) {
				int bitmap = fullUri.lastIndexOf(120);
				Logger.check(bitmap > pos, "The image uri is not correct!");
				expectedWidth = Convert.toInteger(fullUri.substring(pos + 1, bitmap)).intValue();
				expectedHeight = Convert.toInteger(fullUri.substring(bitmap + 1)).intValue();
				uri = fullUri.substring(0, pos);
			} else {
				expectedWidth = 0;
				expectedHeight = 0;
			}

			Bitmap bitmap1 = this.getBitmap(fullUri);
			if(bitmap1 != null) {
				return bitmap1;
			} else {
				if(uri.startsWith("file://")) {
					bitmap1 = ImageHelper.fromFile(uri.substring("file://".length()), expectedWidth, expectedHeight);
				} else if(uri.startsWith("res://")) {
					bitmap1 = ImageHelper.fromStream(MyImageService.class.getResourceAsStream(uri.substring("res://".length())), expectedWidth, expectedHeight);
				} else if((uri.startsWith("http://") || uri.startsWith("https://")) && bitmap1 == null) {
					File bmpFile = this.getCacheOf(fullUri);
					if(bmpFile.exists()) {
						bitmap1 = ImageHelper.fromFile(bmpFile, expectedWidth, expectedHeight);
					}

					if(bitmap1 == null) {
						HttpSession session = (HttpSession)this.sessions.get(uri);
						if(session == null) {
							Logger.check(this.httpService != null, "The httpService must not be null!");
							session = this.httpService.download(uri, bmpFile);
							session.start();
							this.sessions.put(uri, session);
							session.getStateChangedEvent().addEventHandler(new IEventHandler() {


								@Override
								public void process(Object o, EventArgs eventArgs) {
									HttpSession.StateChangedEventArgs args = (HttpSession.StateChangedEventArgs) eventArgs;

									if(args.getState() == 6 && MyImageService.this.allImageHandlers.containsKey(fullUri)) {
										Bitmap bitmap = null;

										try {
											bitmap = ImageHelper.fromFile(args.getSession().getFile(), expectedWidth, expectedHeight);
										} catch (OutOfMemoryError var7) {
											Logger.error(var7);
											MyImageService.this.clearCache();

											try {
												bitmap = ImageHelper.fromFile(args.getSession().getFile(), expectedWidth, expectedHeight);
											} catch (Throwable var6) {
												Logger.error(var6);
											}
										}

										if(bitmap == null) {
											if(args.getSession().getError() != null) {
												Logger.error("Fails to download image (" + args.getSession().getUrl() + ") : " + args.getSession().getError().getMessage());
											}

											if(args.getSession().isDownloaded()) {
												Logger.error("Fails to load downloaded image (" + args.getSession().getUrl() + ")");
											}
										} else {
											ImageEventArgs iea = new ImageEventArgs(fullUri, bitmap, args.getSession().getError());
											IEvent evt = (IEvent)MyImageService.this.allImageHandlers.get(fullUri);
											evt.fireEvent((Object)null, iea);
										}
									}
								}


							});
							Logger.debug("Image " + uri + " has been added into the downloading queue. ");
						}
					}
				}

				if(bitmap1 != null) {
					this.bitmapCache.put(fullUri, bitmap1);
				}

				return bitmap1;
			}
		}
	}

	public boolean isRemote(String uri) {
		return uri.startsWith("http://") || uri.startsWith("https://");
	}

	public String getFullUri(String uri, int width, int height) {
		return uri == null?null:(width != 0 && height != 0?uri + "#" + width + "x" + height:uri);
	}

	public void bind(String fullUri, IEventHandler<ImageEventArgs> handler) {
		Bitmap bitmap = this.getBitmap(fullUri);
		if(bitmap != null) {
			ImageEventArgs args = new ImageEventArgs(fullUri, bitmap, (Throwable)null);
			handler.process(this, args);
		} else {
			this.loadBitmap(fullUri);
			this.addImageEventHandler(fullUri, handler);
		}

	}

	public void bind(String fullUri, ImageView view) {
		Logger.check(view != null, "Parameter view cannot be null !");
		Bitmap bitmap = this.getBitmap(fullUri);
		if(bitmap != null) {
			view.setImageBitmap(bitmap);
		} else {
			this.loadBitmap(fullUri);
			MyImageService.ImageViewRefreshHandler handler = null;
			Iterator it = this.imageViewHandlers.iterator();

			while(it.hasNext()) {
				MyImageService.ImageViewRefreshHandler h = (MyImageService.ImageViewRefreshHandler)it.next();
				if(h.getImageView() == null) {
					it.remove();
				} else if(h.getImageView() == view) {
					handler = h;
					break;
				}
			}

			if(handler == null) {
				handler = new MyImageService.ImageViewRefreshHandler(view);
				this.imageViewHandlers.add(handler);
			}

			this.addImageEventHandler(fullUri, handler);
		}

	}

	public void bind(String fullUri, IContainerBinder binder, int index) {
		Logger.check(binder != null && index != -1, "Parameter binder and item cannot be null !");
		Bitmap bitmap = this.loadBitmap(fullUri);
		if(bitmap == null) {
			MyImageService.BinderRefreshHandler handler = null;
			Iterator it = this.binderHandlers.iterator();

			while(it.hasNext()) {
				MyImageService.BinderRefreshHandler h = (MyImageService.BinderRefreshHandler)it.next();
				if(h.binder.getContainerView() == null) {
					it.remove();
				} else if(h.binder == binder && h.index == index) {
					handler = h;
					break;
				}
			}

			if(handler == null) {
				handler = new MyImageService.BinderRefreshHandler(binder, index);
				this.binderHandlers.add(handler);
			}

			this.addImageEventHandler(fullUri, handler);
		}

	}

	public void start() {
	}

	public void stop() {
		this.allImageHandlers.clear();
		this.imageViewHandlers.clear();
		this.binderHandlers.clear();
	}

	public Class<?>[] getDependencyServices() {
		return new Class[]{IHttpService.class, ICacheService.class};
	}

	public boolean canStop() {
		return true;
	}

	class BinderRefreshHandler implements IEventHandler<ImageEventArgs> {
		public IContainerBinder binder;
		public int index;

		public BinderRefreshHandler(IContainerBinder binder, int index) {
			this.binder = binder;
			this.index = index;
		}

		public void process(Object sender, ImageEventArgs args) {
			if(this.binder.getContainerView() != null && this.binder.getViewOf(this.index) != null) {
				this.binder.getItemBinder().onCreateItemView(this.binder.getViewOf(this.index), this.index, this.binder);
			}

		}
	}

	class ImageViewRefreshHandler implements IEventHandler<ImageEventArgs> {
		WeakReference<ImageView> refImageView;

		public ImageViewRefreshHandler(ImageView iv) {
			this.refImageView = new WeakReference(iv);
		}

		public ImageView getImageView() {
			return (ImageView)this.refImageView.get();
		}

		public void process(Object sender, ImageEventArgs args) {
			if(this.refImageView.get() != null) {
				((ImageView)this.refImageView.get()).setImageBitmap(args.getBitmap());
			}

		}
	}


}
