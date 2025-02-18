function log() {
	for (var i = 0; i < arguments.length; i++) {
		console.log(arguments[i]);
	}
}

(function($) {
	Toast = {
		show: function(json) {
			var message = json ? json.message : undefined;
			if (Utils.isNotEmpty(message)) {
				var x = document.getElementById("toast");
				var c = "show";
				if (json.styleClass) {
					c += " " + json.styleClass;
				}
				x.innerHTML = message;
			    x.className = c;
			    setTimeout(function(){ x.className = x.className.replace(c, ""); }, 6000);
			}
		}	
	};
	Progress = {
		onAjaxEvent: function(callback) {
			return function(data) {
				switch (data.status) {
					case 'begin':
						Device.Progress.show();
					break;
					case 'success':
						if (callback) {
							callback()
						}
					break;
					case 'complete':
						Device.Progress.hide();
					break;
				}
			}
		}
	};
	Ajax = {
		onEvent: function(begin, complete, success) {
			return function(data) {
				switch (data.status) {
					case 'begin':
						if (begin) {
							begin();
						}
					break;
					case 'complete':
						if (complete) {
							complete();
						}
					break;
					case 'success':
						if (success) {
							success();
						}
					break;
				}
			}
		},
		onBegin: function(callback) {
			return function(data) {
				if (data.status == 'begin') {
					callback();
				}
			}
		},
		onSuccess: function(callback) {
			return function(data) {
				if (data.status == 'success') {
					callback();
				}
			}
		},
		onComplete: function(callback) {
			return function(data) {
				if (data.status == 'complete') {
					callback();
				}
			}
		}
	};
FileUpload = {
		bind: function(sel, callback) {
			var url = Utils.getContextPath() + '/fileupload';
			$(sel).fileupload({
				url: url,
				dataType: 'json',
				add: function(e, data) {
					var files = data.files;
					var valid = true;
					if (Utils.isNotEmpty(files)) {
						for (var i = 0; i < files.length; i++) {
							var file = files[i];
							if (!(/\.(gif|jpg|jpeg|png)$/i).test(file.name)) {
								valid = false;
								break;
							}
						}
					}
					if (valid) {
						View.block();
						if (callback) {
							callback('add', data);
						}
						data.submit();
					}
					else {
						alert('Imagem invalida! Utilize arquivos com extensao gif, jpg, jpeg e png.')
					}
				},
				fail: function(e, data) {
					View.unblock();
					var xhr = data.jqXHR;
					if (xhr) {
						alert(xhr.responseText);
					}
					if (callback) {
						callback('fail', data);
					}
				},
				done: function(e, data) {
					View.unblock();
					if (callback) {
						callback('done', data);
					}
				}
			});
		},
		select: function(callback) {
			var sel = 'body > input[type=file][name=file]';
			if ($(sel).length == 0) {
				$('<input type="file" name="file" style="display:none" />').appendTo($('body'));
				FileUpload.bind(sel, function(type, data) {
					if (type == 'fail' || type == 'done') {
						$(sel).remove();
					}
					callback(type, data);
				});
			}
			$(sel).click();
		}
	};
})(jQuery);

Utils = {
	getContextPath: function() {
		var path = window.location.pathname;
		return window.location.pathname.substring(0, window.location.pathname.indexOf('/', 2));
	},
	isNotEmpty: function(value) {
		return !Utils.isEmpty(value);
	},
	isEmpty: function(value) {
		if (value) {
			if (Utils.isString(value)) {
				return StringUtils.isEmpty(value);
			}
			else if (Utils.isArray(value)) {
				return value.length == 0;
			}
			else if (Utils.isObject(value)) {
				for (var key in value) {
					if (value.hasOwnProperty(key)) {
						return false;
					}
				}
				return true;
			}
			return false;
		}
		return true;
	},
	isDate: function(value) {
		return Object.prototype.toString.call(value) === '[object Date]';
	},
	isArray: function(value) {
	    return Object.prototype.toString.call(value) === '[object Array]';
	},
	isObject: function(value) {
		return Object.prototype.toString.call(value) === '[object Object]';
	},
	isString: function(value) {
		return Object.prototype.toString.call(value) === '[object String]';
	},
	isNumber: function(value) {
		return typeof value === 'number' && isFinite(value);
	},
	isFunction: function(value) {
		return Object.prototype.toString.call(value) === '[object Function]';
    },
    isNumberic: function(value) {
    	return !isNaN(parseFloat(value)) && isFinite(value);
    }
};

StringUtils = {
	isEmpty: function(value) {
		return StringUtils.trim(value).length == 0;
	},
	trim: function(value) {
		if (value) {
			value = value.replace(/^[\x09\x0a\x0b\x0c\x0d\x20\xa0\u1680\u180e\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200a\u2028\u2029\u202f\u205f\u3000]+|[\x09\x0a\x0b\x0c\x0d\x20\xa0\u1680\u180e\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200a\u2028\u2029\u202f\u205f\u3000]+$/g, '');
	}
	return value || '';
	}
};