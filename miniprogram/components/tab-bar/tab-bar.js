Component({
  properties: {
    currentTab: {
      type: Number,
      value: 0
    }
  },
  data: {
    tabs: [
      { text: "首页", icon: "/static/tabbar/home.png", url: "/pages/index/index" },
      { text: "消息", icon: "/static/tabbar/message.png", url: "/pages/message/index" },
      { text: "扫一扫", icon: "/static/tabbar/socde.png", url: "/pages/scan/index" },
      { text: "我的", icon: "/static/tabbar/info.png", url: "/pages/mine/index" }
    ]
  },
  methods: {
    onSwitch(e) {
      const index = Number(e.currentTarget.dataset.index);
      if (index === this.data.currentTab) return;
      const tab = this.data.tabs[index];
      if (tab) wx.redirectTo({ url: tab.url });
    }
  }
});
