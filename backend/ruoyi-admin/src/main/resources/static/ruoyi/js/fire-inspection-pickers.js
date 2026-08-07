/**
 * 巡查测试 PC 端：楼层 4 列网格 + 年/月/日三列滚动选择
 * 依赖 jQuery。
 */
(function (window, $) {
    'use strict';

    function pad(n) {
        return String(n).padStart(2, '0');
    }

    function daysInMonth(year, month) {
        return new Date(year, month, 0).getDate();
    }

    function buildFloorList() {
        var floors = [];
        var i;
        for (i = -5; i <= -1; i++) {
            floors.push(i + 'F');
        }
        for (i = 1; i <= 99; i++) {
            floors.push(i + 'F');
        }
        return floors;
    }

    function buildYears() {
        var now = new Date().getFullYear();
        var years = [];
        var y;
        for (y = now - 10; y <= now + 1; y++) {
            years.push(y);
        }
        return years;
    }

    function ensureMask($root) {
        var $mask = $root.find('.insp-sheet-mask');
        if (!$mask.length) {
            $mask = $('<div class="insp-sheet-mask"></div>').appendTo($root);
        }
        return $mask;
    }

    function closeMask($mask) {
        $mask.removeClass('open').empty();
    }

    function initFloorPicker(options) {
        var $trigger = $(options.trigger);
        var $hidden = $(options.hidden);
        var $mount = $(options.mount || 'body');
        var floors = buildFloorList();

        function syncDisplay() {
            var v = $hidden.val() || '';
            $trigger.val(v || '');
            $trigger.attr('placeholder', v ? '' : '请选择');
        }

        function openSheet() {
            var current = $hidden.val() || '';
            var $mask = ensureMask($mount);
            var html = ''
                + '<div class="insp-sheet" data-sheet="floor">'
                + '  <div class="insp-sheet-title">请选择一项</div>'
                + '  <div class="insp-floor-scroll"><div class="insp-floor-grid">';
            for (var i = 0; i < floors.length; i++) {
                var f = floors[i];
                html += '<div class="insp-floor-cell' + (f === current ? ' active' : '') + '" data-value="' + f + '">' + f + '</div>';
            }
            html += ''
                + '  </div></div>'
                + '  <div class="insp-floor-cancel">取消</div>'
                + '</div>';
            $mask.html(html).addClass('open');

            $mask.off('click.inspFloor').on('click.inspFloor', function (e) {
                if (e.target === $mask[0]) {
                    closeMask($mask);
                }
            });
            $mask.find('.insp-floor-cancel').on('click', function () {
                closeMask($mask);
            });
            $mask.find('.insp-floor-cell').on('click', function () {
                var val = $(this).data('value');
                $hidden.val(val);
                syncDisplay();
                closeMask($mask);
            });
        }

        $trigger.prop('readonly', true).addClass('insp-picker-trigger');
        $trigger.off('click.inspFloor focus.inspFloor').on('click.inspFloor focus.inspFloor', function (e) {
            e.preventDefault();
            openSheet();
        });
        syncDisplay();
        return { syncDisplay: syncDisplay };
    }

    function initDatePicker(options) {
        var $trigger = $(options.trigger);
        var $hidden = $(options.hidden);
        var $mount = $(options.mount || 'body');
        var years = buildYears();
        var months = [];
        var m;
        for (m = 1; m <= 12; m++) {
            months.push(m);
        }

        var draft = { year: 0, month: 0, day: 0 };

        function parseHidden() {
            var raw = ($hidden.val() || '').trim();
            var datePart = '';
            var timePart = '00:00:00';
            if (raw) {
                var parts = raw.split(/\s+/);
                datePart = parts[0] || '';
                if (parts[1]) {
                    timePart = parts[1].length === 5 ? parts[1] + ':00' : parts[1];
                }
            }
            return { datePart: datePart, timePart: timePart };
        }

        function syncDisplay() {
            var parsed = parseHidden();
            $trigger.val(parsed.datePart || '');
            $trigger.attr('placeholder', parsed.datePart ? '' : '请选择');
        }

        function setDatePart(datePart) {
            var parsed = parseHidden();
            var timePart = parsed.timePart || '00:00:00';
            if (!parsed.datePart && options.defaultNow) {
                var now = new Date();
                timePart = pad(now.getHours()) + ':' + pad(now.getMinutes()) + ':' + pad(now.getSeconds());
            }
            $hidden.val(datePart + ' ' + timePart);
            syncDisplay();
        }

        function ensureNowIfEmpty() {
            if (($hidden.val() || '').trim()) { return; }
            if (!options.defaultNow) { return; }
            var now = new Date();
            var datePart = now.getFullYear() + '-' + pad(now.getMonth() + 1) + '-' + pad(now.getDate());
            var timePart = pad(now.getHours()) + ':' + pad(now.getMinutes()) + ':' + pad(now.getSeconds());
            $hidden.val(datePart + ' ' + timePart);
            syncDisplay();
        }

        function buildDays(year, month) {
            var total = daysInMonth(year, month);
            var days = [];
            var d;
            for (d = 1; d <= total; d++) { days.push(d); }
            return days;
        }

        function renderColumn($col, values, selected, suffix) {
            var html = '<div class="insp-date-spacer"></div>';
            for (var i = 0; i < values.length; i++) {
                var v = values[i];
                html += '<div class="insp-date-item' + (v === selected ? ' active' : '') + '" data-value="' + v + '">' + v + suffix + '</div>';
            }
            html += '<div class="insp-date-spacer"></div>';
            $col.html(html);
        }

        function scrollToValue($col, value) {
            var $item = $col.find('.insp-date-item').filter(function () {
                return Number($(this).data('value')) === Number(value);
            });
            if ($item.length) {
                var top = $item.position().top + $col.scrollTop() - ($col.height() / 2) + ($item.outerHeight() / 2);
                $col.scrollTop(Math.max(0, top));
            }
        }

        function refreshActive($col, value) {
            $col.find('.insp-date-item').each(function () {
                var active = Number($(this).data('value')) === Number(value);
                $(this).toggleClass('active', active);
            });
        }

        function openSheet() {
            ensureNowIfEmpty();
            var parsed = parseHidden();
            var segs = (parsed.datePart || '').split('-');
            var now = new Date();
            draft.year = parseInt(segs[0], 10) || now.getFullYear();
            draft.month = parseInt(segs[1], 10) || (now.getMonth() + 1);
            draft.day = parseInt(segs[2], 10) || now.getDate();
            if (years.indexOf(draft.year) < 0) {
                years.push(draft.year);
                years.sort(function (a, b) { return a - b; });
            }
            var days = buildDays(draft.year, draft.month);
            if (draft.day > days.length) { draft.day = days.length; }

            var $mask = ensureMask($mount);
            var html = ''
                + '<div class="insp-sheet" data-sheet="date">'
                + '  <div class="insp-sheet-hd">'
                + '    <span class="cancel">取消</span>'
                + '    <span class="title">选择日期</span>'
                + '    <span class="confirm">确认</span>'
                + '  </div>'
                + '  <div class="insp-date-cols">'
                + '    <div class="insp-date-col" data-col="year"></div>'
                + '    <div class="insp-date-col" data-col="month"></div>'
                + '    <div class="insp-date-col" data-col="day"></div>'
                + '  </div>'
                + '</div>';
            $mask.html(html).addClass('open');

            var $year = $mask.find('[data-col="year"]');
            var $month = $mask.find('[data-col="month"]');
            var $day = $mask.find('[data-col="day"]');

            function paintDays() {
                var dlist = buildDays(draft.year, draft.month);
                if (draft.day > dlist.length) { draft.day = dlist.length; }
                renderColumn($day, dlist, draft.day, '日');
                scrollToValue($day, draft.day);
            }

            renderColumn($year, years, draft.year, '年');
            renderColumn($month, months, draft.month, '月');
            paintDays();
            setTimeout(function () {
                scrollToValue($year, draft.year);
                scrollToValue($month, draft.month);
                scrollToValue($day, draft.day);
            }, 0);

            $mask.off('click.inspDate').on('click.inspDate', function (e) {
                if (e.target === $mask[0]) { closeMask($mask); }
            });
            $mask.find('.cancel').on('click', function () { closeMask($mask); });
            $mask.find('.confirm').on('click', function () {
                var datePart = draft.year + '-' + pad(draft.month) + '-' + pad(draft.day);
                setDatePart(datePart);
                closeMask($mask);
            });

            $year.on('click', '.insp-date-item', function () {
                draft.year = Number($(this).data('value'));
                refreshActive($year, draft.year);
                paintDays();
            });
            $month.on('click', '.insp-date-item', function () {
                draft.month = Number($(this).data('value'));
                refreshActive($month, draft.month);
                paintDays();
            });
            $day.on('click', '.insp-date-item', function () {
                draft.day = Number($(this).data('value'));
                refreshActive($day, draft.day);
            });

            function onScrollSnap($col, values, key) {
                var timer = null;
                $col.on('scroll', function () {
                    clearTimeout(timer);
                    timer = setTimeout(function () {
                        var mid = $col.offset().top + $col.height() / 2;
                        var best = null;
                        var bestDist = Infinity;
                        $col.find('.insp-date-item').each(function () {
                            var $it = $(this);
                            var center = $it.offset().top + $it.outerHeight() / 2;
                            var dist = Math.abs(center - mid);
                            if (dist < bestDist) {
                                bestDist = dist;
                                best = Number($it.data('value'));
                            }
                        });
                        if (best != null && draft[key] !== best) {
                            draft[key] = best;
                            refreshActive($col, best);
                            if (key === 'year' || key === 'month') { paintDays(); }
                        }
                    }, 80);
                });
            }
            onScrollSnap($year, years, 'year');
            onScrollSnap($month, months, 'month');
            onScrollSnap($day, buildDays(draft.year, draft.month), 'day');
        }

        $trigger.prop('readonly', true).addClass('insp-picker-trigger');
        $trigger.off('click.inspDate focus.inspDate').on('click.inspDate focus.inspDate', function (e) {
            e.preventDefault();
            openSheet();
        });
        if (options.defaultNow) { ensureNowIfEmpty(); } else { syncDisplay(); }
        return { syncDisplay: syncDisplay, ensureNowIfEmpty: ensureNowIfEmpty };
    }

    window.FireInspectionPickers = {
        initFloorPicker: initFloorPicker,
        initDatePicker: initDatePicker,
        buildFloorList: buildFloorList
    };
})(window, jQuery);
