// get_item_list.sc v1 - by 51mayday

// Gets list of all items with stackability and saves it as a .json file

__config() -> {
    'commands' -> {
        '' -> 'runner'
    };
};

runner() -> (
    item_list = item_list();
    output_dict = {};
    for(item_list,
        output_dict:_ = {'stack_size' -> stack_limit(_)};
    );
    print(player(), output_dict);

    output_json = encode_json(output_dict);

    write_file('items_stackability_' + str(system_info('game_version')), 'shared_json', output_dict);
);