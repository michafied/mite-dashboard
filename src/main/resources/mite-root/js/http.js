function http(loading) {
  loading = (typeof loading !== 'function') ? ()=>console.log("loading noop") : loading;
  return {
    loading: loading,
    post: function(resource, data, $2xx, $4xx, $error){
      if (typeof resource !== 'string') {
        return;
      }
      $2xx = (typeof $2xx !== 'function') ? ()=>console.log("2xx noop") : $2xx;
      $4xx = (typeof $4xx !== 'function') ? ()=>console.log("4xx noop") : $4xx;
      $error = (typeof $error !== 'function') ? ()=>console.log("error noop") : $error;
      this.loading();
      var xhr = new XMLHttpRequest();
      xhr.onreadystatechange = function() {
        if (this.readyState == 4) {
          if((xhr.status/100).toPrecision(1) == 2) {
            $2xx(this.response)
          } else if((xhr.status/100).toPrecision(1) == 4) {
            $4xx(this.response)
          } else {
            $error(this)
          }
        }
      };

      xhr.open("POST", resource, true);
      xhr.setRequestHeader('Content-Type', 'application/json; charset=utf-8');
      xhr.send(JSON.stringify(data));
    },
    get: function(resource, $2xx, $4xx, $error){
      if (typeof resource !== 'string') {
        return;
      }
      $2xx = (typeof $2xx !== 'function') ? ()=>console.log("2xx noop") : $2xx;
      $4xx = (typeof $4xx !== 'function') ? ()=>console.log("4xx noop") : $4xx;
      $error = (typeof $error !== 'function') ? ()=>console.log("error noop") : $error;
      this.loading();
      var xhr = new XMLHttpRequest();
      xhr.onreadystatechange = function() {
        if (this.readyState == 4) {
          if((xhr.status/100).toPrecision(1) == 2) {
            $2xx(this.response)
          } else if((xhr.status/100).toPrecision(1) == 4) {
            $4xx(this.response)
          } else {
            $error(this)
          }
        }
      };

      xhr.open("GET", resource, true);
      xhr.setRequestHeader('Content-Type', 'application/json; charset=utf-8');
      xhr.send('');
    },
    patch: function(resource, data, $2xx, $4xx, $error){
      if (typeof resource !== 'string') {
        return;
      }
      $2xx = (typeof $2xx !== 'function') ? ()=>console.log("2xx noop") : $2xx;
      $4xx = (typeof $4xx !== 'function') ? ()=>console.log("4xx noop") : $4xx;
      $error = (typeof $error !== 'function') ? ()=>console.log("error noop") : $error;
      this.loading();
      var xhr = new XMLHttpRequest();
      xhr.onreadystatechange = function() {
        if (this.readyState == 4) {
          if((xhr.status/100).toPrecision(1) == 2) {
            $2xx(this.response)
          } else if((xhr.status/100).toPrecision(1) == 4) {
            $4xx(this.response)
          } else {
            $error(this)
          }
        }
      };

      xhr.open("PATCH", resource, true);
      xhr.setRequestHeader('Content-Type', 'application/json; charset=utf-8');
      xhr.send(JSON.stringify(data));
    },
  }
}
