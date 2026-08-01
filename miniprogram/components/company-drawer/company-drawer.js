const { api } = require("../../api/index.js");

Component({
  properties: {
    visible: { type: Boolean, value: false },
    currentCompanyId: { type: null, value: null }
  },
  data: {
    keyword: "",
    loading: false,
    list: [],
    filtered: []
  },
  observers: {
    visible(v) {
      if (v) this.loadList();
    }
  },
  methods: {
    onClose() {
      this.triggerEvent("close");
    },
    onKeyword(e) {
      const keyword = (e.detail.value || "").trim();
      this.setData({ keyword });
      this.applyFilter();
    },
    applyFilter() {
      const kw = (this.data.keyword || "").toLowerCase();
      const filtered = !kw
        ? this.data.list.slice()
        : this.data.list.filter((c) => {
            const name = (c.companyName || "").toLowerCase();
            const addr = (c.address || "").toLowerCase();
            return name.indexOf(kw) >= 0 || addr.indexOf(kw) >= 0;
          });
      this.setData({ filtered });
    },
    async loadList() {
      this.setData({ loading: true });
      try {
        const res = await api.getMyCompanyList();
        const list = (res && res.data) || [];
        this.setData({ list, loading: false });
        this.applyFilter();
      } catch (e) {
        this.setData({ list: [], filtered: [], loading: false });
      }
    },
    onSelect(e) {
      const id = e.currentTarget.dataset.id;
      const company = this.data.list.find((c) => String(c.companyId) === String(id));
      if (company) this.triggerEvent("select", { company });
    },
    noop() {}
  }
});
