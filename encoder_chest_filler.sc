// fills chests in an encoder according to itemlist files
// sets chest ss to 2
// if an itemtype is not valid it will fill that slot with a placeholder tropical_fish renamed to the intended item
// if itemlist has more than 54 itemtypes, it will preserve the rest and move to the next chest in the row

__config()-> {
    'commands' -> {'<from_pos> <to_pos> <item_lists>' -> 'encoderRunner'      
    },
    'arguments' -> {
        'item_lists' -> {
            'type' -> 'text',
            'suggester' -> _(args) -> (
                input = args:'item_lists';
                entries = split('\\s+', input);
                item_lists = map(list_files('item_lists', 'shared_text'), slice(_, length('item_lists') + 1));
                if(entries && slice(input, -1) != ' ', delete(entries, -1));
                return(if(entries, map(item_lists, str('%s %s', join(' ', entries), _)), item_lists));
            ),
            'case_sensitive' -> false
        },
        'from_pos' -> {
            'type' -> 'pos',
            'loaded' -> true
        },
        'to_pos' -> {
            'type' -> 'pos',
            'loaded' -> true
        }
    },
    'scope' -> 'global'
};

global_stackable_dummy_item = ['red_stained_glass', '{display:{Name:\'"blocker"\'}}'];
global_unstackable_dummy_item = ['shears', null];

_readItemList(item_list) -> (
    item_list_path = str('item_lists/%s', item_list);
    entries = map(read_file(item_list_path, 'shared_text'), [_, _:1 || null]);

    return(entries)
);

_scanStrip(from_pos, to_pos) -> (
    [x1, y1, z1] = from_pos;
    [x2, y2, z2] = to_pos;
    [dx, dy, dz] = map(to_pos - from_pos, if(_ < 0, -1, 1));

    if(
        x1 != x2,
            return(map(range(x1, x2 + dx, dx), block([_, y1, z1]))),
        y1 != y2,
            return(map(range(y1, y2 + dy, dy), block([x1, _, z1]))),
        z1 != z2,
            return(map(range(z1, z2 + dz, dz), block([x1, y1, _])))
    );
    return([block(from_pos)]);
);

_isInvalidItem(item) -> (
    return(item_list()~item == null || (item == 'air') || stack_limit(item) == 1);
);

_encoderChest(chest, items) -> (
    signal_strength = 1;
    amount = ceil(inventory_size(chest)*64/14*signal_strength);
    loop(inventory_size(chest),
        inventory_set(chest, _, 0);
        [item, nbt] = if(_ < length(items), items:_, global_stackable_dummy_item);
        
        if(_isInvalidItem(item), [item, nbt] = ['tropical_fish', str('{display:{Name:\'"%s"\'}}', item)]);
        
        inventory_set(chest, _, 2, item, nbt);

        amount += -2*(64/stack_limit(item));
        
        if(_ == 53,
            slot = inventory_size(chest);
            while(amount > 0,
                extra_fill = min(amount, 62);
                [item, count, nbt] = inventory_get(chest, slot - _ - 1);
                inventory_set(chest, slot - _ - 1, count + extra_fill);
                amount += -extra_fill*(64/stack_limit(item));
            );
        );
    )
);

encoderRunner(from_pos, to_pos, item_list_string) -> (
    item_lists = split(' ', item_list_string);
    item_list_contents = map(item_lists, _readItemList(_));

    i = 0;
    affected_blocks = _scanStrip(from_pos, to_pos);
    for(item_list_contents,
        items = _;
        while(length(items) > 0, 10, 
            block = affected_blocks:i;
            if(block != 'chest', i += 1; print('f'); continue(););
            to_chest = slice(items, 0, if(length(items) < 54, length(items), 54););

            _encoderChest(block, to_chest);

            i += 1;
            loop(length(to_chest), 
                delete(items, 0);
            );
        );
    );
);