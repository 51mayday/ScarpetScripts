// Reports motion an position of all item entities within distance of the player executing the command

__config() -> {
    'commands' -> {
        '<origin_pos> <range>' -> 'run',
    },
	'arguments' -> {
		'range' -> {
			'type' -> 'int',
			'min' -> 4,
			'max' -> 128,
			'suggest' -> [16, 32, 64]
		}
	},
    'scope'-> 'player'
};

run(origin_pos, range) - (
	items = entity_area(item, origin_pos, range, range, range);
	for(items, 
		print(player(), str('%s: \n Pos: %f, %f, %f \n Mot: %f, %f, %f', _ ~ 'item', _ ~ 'x', _ ~ 'y', _ ~ 'z', _ ~ 'MotX', _ ~ 'MotY', _ ~ 'MotZ'));
	);
);