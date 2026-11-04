/* Live "share of total" for the matching configuration page. No requests; it only reads the form. */
(function () {
    var rows = document.querySelectorAll('[data-criterion-row]');

    function weightOf(row) {
        var weight = parseFloat(row.querySelector('[data-weight]').value);
        return isNaN(weight) || weight < 0 ? 0 : weight;
    }

    function isOn(row) {
        return row.querySelector('[data-enabled]').checked;
    }

    function update() {
        var total = 0;
        var i;
        for (i = 0; i < rows.length; i++) {
            if (isOn(rows[i])) {
                total += weightOf(rows[i]);
            }
        }
        for (i = 0; i < rows.length; i++) {
            var share = 0;
            if (total > 0 && isOn(rows[i])) {
                share = weightOf(rows[i]) / total * 100;
            }
            rows[i].querySelector('[data-share]').textContent = Math.round(share) + '%';
            rows[i].querySelector('[data-fill]').style.width = share + '%';
        }
    }

    for (var i = 0; i < rows.length; i++) {
        rows[i].querySelector('[data-enabled]').addEventListener('change', update);
        rows[i].querySelector('[data-weight]').addEventListener('input', update);
    }
    update();
})();