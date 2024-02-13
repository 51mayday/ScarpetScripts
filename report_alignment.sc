// Report Alignment v1.2 by 51mayday
// Reports motion an position of all item entities within distance of the player executing the command

__config() -> {
    'commands' -> {
        '<origin_pos> <range>' -> 'get_info'
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
		print(player(), format('wb ' + (_ ~ 'item'):0 + ':', 'w ' + str('\n ID: %d (mod4: %d) \n Count: %d \n Age: %d \n Pos: %f, %f, %f \n Mot: %f, %f, %f', , _ ~ 'id', (_ ~ 'id') % 4,(_ ~ 'item'):1, _ ~ 'age', _ ~ 'x', _ ~ 'y', _ ~ 'z', _ ~ 'motion_x', _ ~ 'motion_y', _ ~ 'motion_z')));
	);
);
