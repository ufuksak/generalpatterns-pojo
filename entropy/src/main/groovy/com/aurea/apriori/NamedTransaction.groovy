package com.aurea.apriori

import de.mrapp.apriori.Transaction

class NamedTransaction implements Transaction<NamedItem> {
    private Collection<NamedItem> items

    NamedTransaction(Collection<NamedItem> items) {
        this.items = items
    }

    @Override
    Iterator<NamedItem> iterator() {
        items.iterator()
    }
}
