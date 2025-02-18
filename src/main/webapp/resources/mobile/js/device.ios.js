(function($) {
	$(function() {
		Device = $.extend(Device, {}, {
			Flash : {
				hash : {},
				get : function(key) {
					var value = Device.Flash.hash[key];
					delete Device.Flash.hash[key];
					return value;
				},
				set : function(key, value) {
					if (value != undefined) {
						Device.Flash.hash[key] = value;
					} else {
						delete Device.Flash.hash[key];
					}
				}
			},
			Scheme : {
				initialize: 'initialize',
				toastShow: 'toast.show',
				imageOpen: 'image.open',
				imageUpload: 'image.upload',
				progressShow: 'progress.show',
				progressHide: 'progress.hide'
			},
			Progress: {
				show: function() {
					var scheme = Device.Scheme.progressShow;
					window.location = scheme + '://';
				},
				hide: function() {
					var scheme = Device.Scheme.progressHide;
					window.location = scheme + '://';
				}
			},
			Image: {
				open: function(src) {
					var scheme = Device.Scheme.imageOpen;
					Device.Flash.set(scheme, src);
					window.location = scheme + '://';
				},
				upload: function(config) {
					var value;
					var scheme = Device.Scheme.imageUpload;
					if (config) {
						value = JSON.stringify(config);
					}
					Device.Flash.set(scheme, value);
					window.location = scheme + '://';
		      	}
			},
			getArgs : function(scheme) {
				return Device.Flash.get(scheme);
			},
			initialize : function(view) {
				var value;
				var scheme = Device.Scheme.initialize;
				if (view) {
					value = JSON.stringify(view);
				}
				Device.Flash.set(scheme, value);
				window.location = scheme + '://';
			},
			Toast: {
				show: function(message) {
					var scheme = Device.Scheme.toastShow;
					Device.Flash.set(scheme, message);
					window.location = scheme + '://';
				}
			}
		});
		if (Device.onReady) {
			Device.onReady();
		}
	});
})(jQuery);