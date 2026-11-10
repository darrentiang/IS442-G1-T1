const userSearch = document.getElementById("userSearch");
const userSelect = document.getElementById("userId");
const userSearchResults = document.getElementById("userSearchResults");
const userSearchStatus = document.getElementById("userSearchStatus");
const userOptions = Array.from(userSelect.querySelectorAll("[data-user-option]"));

function normalizeText(text) {
    return text.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase();
}

function editDistanceAtMostOne(first, second) {
    if (Math.abs(first.length - second.length) > 1) {
        return false;
    }

    let firstIndex = 0;
    let secondIndex = 0;
    let differences = 0;

    while (firstIndex < first.length && secondIndex < second.length) {
        if (first[firstIndex] === second[secondIndex]) {
            firstIndex++;
            secondIndex++;
        } else {
            differences++;
            if (differences > 1) {
                return false;
            }
            if (first.length > second.length) {
                firstIndex++;
            } else if (second.length > first.length) {
                secondIndex++;
            } else {
                firstIndex++;
                secondIndex++;
            }
        }
    }

    if (firstIndex < first.length || secondIndex < second.length) {
        differences++;
    }

    return differences <= 1;
}

function matchesSearch(option, query) {
    const text = normalizeText(option.textContent);
    if (text.includes(query)) {
        return true;
    }

    const words = text.split(/\s+/);
    const queryWords = query.split(/\s+/);
    return queryWords.every(function (queryWord) {
        return words.some(function (word) {
            return word.includes(queryWord)
                || (queryWord.length >= 5 && editDistanceAtMostOne(queryWord, word));
        });
    });
}

function renderSearchResults() {
    const query = normalizeText(userSearch.value.trim());
    const matches = query === ""
        ? []
        : userOptions.filter(function (option) {
            return matchesSearch(option, query);
        });

    if (query !== "" && userSelect.value !== ""
            && !matches.some(function (option) {
                return option.value === userSelect.value;
            })) {
        userSelect.value = "";
    }

    userSearchResults.replaceChildren();
    matches.forEach(function (option) {
        const item = document.createElement("li");
        const button = document.createElement("button");
        button.type = "button";
        button.className = "btn btn-quiet user-search-result";
        button.textContent = option.textContent;
        button.addEventListener("click", function () {
            userSelect.value = option.value;
            userSearch.value = "";
            renderSearchResults();
            userSelect.focus();
        });
        item.appendChild(button);
        userSearchResults.appendChild(item);
    });

    userSearchResults.hidden = matches.length === 0;
    userSearchStatus.hidden = query === "" || matches.length > 0;
}

userSearch.addEventListener("input", renderSearchResults);
