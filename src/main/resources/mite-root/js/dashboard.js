function setup() {
var store = { update : 0 };

var createProject = new Vue({
  el: '#createProject',
  data: {
    customers: [],
    name: "",
    budget: 1,
    customer: "",
    feedback: "",
    error: false,
    store: store
  },
  created: function () {
    var self = this;
    http()
      .get(
        "./customers",
        response => {
          self.customers = JSON.parse(response);
        }
      );
    },
    methods: {
      send: function (event) {
        var self = this;
        window.setTimeout(function(){
          self.feedback = "";
          self.error = false;
        }, 3000);

        http(() => { self.feedback = "loading"; }).post(
          "./projects"+window.location.search,
          {
            "name": self.name,
            "customerId": parseInt(self.customer, 10),
            "budget": parseInt(self.budget, 10)
          },
          response => {
            self.feedback = "done";
            self.store.update++;
          },
          response => {
            self.error = true;
            self.feedback = JSON.parse(response).error;
          },
          xhr => {
            self.error = true;
            self.feedback = "error: "+xhr.status;
          }
        );
      }
    }
});

var projectList = new Vue({
  el: '#projectList',
  data: {
    vProjects: [],
    projectTimes: {},
    store: store
  },
  methods: {
    loadTime: function (id) {
      if(id !== undefined) {
        var self = this;
        http().get(
          "./times/"+id,
          response => {
            Vue.set(self.projectTimes, id, JSON.parse(response).hours)
          }
        );
      }
    },
    refresh: function () {
      var self = this;
      http().get(
        "./vProjects"+window.location.search,
        response => {
          var vProjects = JSON.parse(response);
          self.vProjects = vProjects;
          for(i=0; i < vProjects.length; ++i){
            for(j=0; j < vProjects[i].children.length; ++j){
              self.loadTime(vProjects[i].children[j].id);
            }
          }
        }
      );
    },
    vBudget: function (vProject) {
      return vProject.children.map(p => p.budget).reduce((acc, it) => acc += it, 0);
    },
    vTimeUsed: function (vProject) {
      return vProject.children.map(p => this.projectTimes[p.id]).reduce((acc, it) => acc += it, 0);
    }
  },
  created: function () {
    this.refresh();
  },
  watch: {
    store: {
      deep: true,
      handler (val, oldVal) {
        console.log("watched:"+oldVal+" -> "+val);
        this.refresh();
      }
    }
  }
});

}
