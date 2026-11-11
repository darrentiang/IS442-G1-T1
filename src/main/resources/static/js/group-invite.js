// Group detail page: copies the private group's invite link when the leader presses "Copy link".
const copyButton = document.getElementById("copyInviteLink");

if (copyButton) {
    const linkInput = document.getElementById(copyButton.dataset.copyTarget);
    const originalText = copyButton.textContent;

    function showCopied() {
        copyButton.textContent = "Copied";
        setTimeout(function () {
            copyButton.textContent = originalText;
        }, 2000);
    }

    function selectLink() {
        linkInput.focus();
        linkInput.select();
    }

    copyButton.addEventListener("click", function () {
        if (navigator.clipboard && window.isSecureContext) {
            navigator.clipboard.writeText(linkInput.value).then(showCopied, selectLink);
        } else {
            // Without the clipboard API, select the link so the user can press Ctrl+C.
            selectLink();
            if (document.execCommand("copy")) {
                showCopied();
            }
        }
    });

    linkInput.addEventListener("focus", function () {
        linkInput.select();
    });
}
