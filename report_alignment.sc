// Reports motion an position of all item entities within distance of the player executing the command
// Age is now reported from the nbt data of the item entity, rather than the entities "internal age counter" (whatever that means)

__config() -> {
    'commands' -> {
        'start <origin_pos> <range>' -> 'get_info'
    },
	'arguments' -> {
		'range' -> {
			'type' -> 'int',
			'min' -> 1,
			'max' -> 128,
			'suggest' -> [16, 32, 64]
		}
	},
    'scope'-> 'global'
};

get_info(test_pos, range) -> (
	items = entity_area('item', test_pos, [range, range, range]);
	print(player(), 'Getting alignments...');
	for(items, 
		print(player(), str('%s: \n ID: %d (mod4: %d) \n Count: %d \n Age: %d \n Pos: %f, %f, %f \n Mot: %f, %f, %f', (_ ~ 'item'):0, _ ~ 'id', (_ ~ 'id') % 4,(_ ~ 'item'):1, query(_, 'nbt', 'Age'), _ ~ 'x', _ ~ 'y', _ ~ 'z', _ ~ 'motion_x', _ ~ 'motion_y', _ ~ 'motion_z'));
	);
);