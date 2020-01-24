function setup() {

var projectDetails = new Vue({
  el: '#projectDetails',
  data: {
    project: {},
    projectTime: 0
  },
  methods: {
    loadTime: function () {
      if(pId !== undefined) {
        var self = this;
        http().get(
          "./times/"+pId,
          response => {
            self.projectTime = JSON.parse(response).hours
          }
        );
      }
    },
    refresh: function () {
      var self = this;
      http().get(
        "./projects/"+pId,
        response => {
          self.project = JSON.parse(response);
          self.loadTime();
        },
        err => {
          window.location.href = "./dashboard";
        },
        err => {
          window.location.href = "./dashboard";
        }
      );
    },
    archive: function () {
      var self = this;
      http().patch(
        "./projects/"+pId,
        {
          "archived": true
        },
        ignore => {
          self.refresh();
        }
      );
    },
    unArchive: function () {
      var self = this;
      http().patch(
        "./projects/"+pId,
        {
          "archived": false
        },
        ignore => {
          self.refresh();
        }
      );
    }
  },
  created: function () {
    this.refresh();
  }
});

}
