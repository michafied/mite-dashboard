function setup() {

var vProjectDetails = new Vue({
  el: '#vProjectDetails',
  data: {
    vProject: {},
    projectTimes: {},
    timeSum: 0
  },
  methods: {
    loadTime: function (project) {
      if(project !== undefined) {
        var self = this;
        http().get(
          "./times/"+project.id+"?split",
          response => {
            Vue.set(project, "times", JSON.parse(response))
          }
        );
      }
    },
    refresh: function () {
      var self = this;
      http().get(
        "./vProjects/"+pId,
        response => {
          var vProject = JSON.parse(response);
          self.vProject = vProject;
          for(j=0; j < vProject.children.length; ++j){
            self.loadTime(vProject.children[j]);
          }
        }
      );
    },
    vBudget: function () {
      if (this.vProject.children != undefined) {
        return this.vProject.children
          .map(p => p.budget != undefined ? p.budget : 0)
          .reduce((acc, it) => acc += it, 0);
      }
      return -1;
    },
    vUsed: function () {
      if (this.vProject.children != undefined) {
        return this.vProject.children
          .flatMap(p => p.times != undefined ? p.times : [])
          .map(t => t.hours != undefined ? t.hours : 0)
          .reduce((acc, it) => acc += it, 0);
      }
      return -1;
    },
    pUsed: function (project) {
      if (project != undefined && project.times != undefined) {
        return project.times
          .map(t => t.hours != undefined ? t.hours : 0)
          .reduce((acc, it) => acc += it, 0);
      }
      return -1;
    }
  },
  created: function () {
    this.refresh();
  }
});

}
