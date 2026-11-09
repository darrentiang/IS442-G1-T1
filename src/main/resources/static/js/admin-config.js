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
/* Loading state for the slow buttons (Simulate, Find best settings): show progress and block double submits. */
(function () {
    var buttons = document.querySelectorAll('button');

    function startLoading(button) {
        button.setAttribute('data-original-text', button.textContent);
        button.textContent = button.getAttribute('data-loading-text');
        button.classList.add('is-loading');
        document.querySelector('main').setAttribute('aria-busy', 'true');
        for (var i = 0; i < buttons.length; i++) {
            buttons[i].disabled = true;
        }
    }

    function stopLoading() {
        for (var i = 0; i < buttons.length; i++) {
            var original = buttons[i].getAttribute('data-original-text');
            if (original !== null) {
                buttons[i].textContent = original;
                buttons[i].classList.remove('is-loading');
            }
            buttons[i].disabled = false;
        }
        document.querySelector('main').removeAttribute('aria-busy');
    }

    function onSubmit(event) {
        var button = event.submitter;
        if (button && button.hasAttribute('data-loading-text')) {
            // Wait a tick so the browser sends the form before the buttons are disabled.
            setTimeout(function () {
                startLoading(button);
            }, 0);
        }
    }

    var forms = document.querySelectorAll('form');
    for (var i = 0; i < forms.length; i++) {
        forms[i].addEventListener('submit', onSubmit);
    }
    // Coming back with the Back button can restore the page as it was left, so undo the loading state.
    window.addEventListener('pageshow', stopLoading);
})();
