function setup() {

var projectDetails = new Vue({
  el: '#projectDetails',
  data: {
    project: {},
    projectTimes: [],
    timeSum: 0,
    vProjects: []
  },
  methods: {
    loadTime: function () {
      if(pId !== undefined) {
        var self = this;
        http().get(
          "./times/"+pId+"?split",
          response => {
            var pt = JSON.parse(response);
            self.projectTimes = pt;
            self.timeSum = pt.map(t => t.hours).reduce((acc, it) => acc += it, 0);
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
      http().get(
        "./vProjects?shallow",
        response => {
          self.vProjects = JSON.parse(response);
        }
      )
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
    },
    assign: function(event) {
      var self = this;
      http().post(
        "./vProjects/mapping",
        {
          "vId": parseInt(event.target.value, 10),
          "pId": pId
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
