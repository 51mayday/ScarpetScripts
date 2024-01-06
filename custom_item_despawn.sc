// Custom Item Despawn Timer by 51 mayday, adapted from code by CommandLeo
// Allows user to set a custom item despawn timer for all items in their world

__config() -> {
    'commands' -> {
        '<lifetime>' -> 'set_lifetime',
        'reset' -> 'reset_lifetime'
    },
    'arguments' -> {
        'lifetime' -> {
            'type' -> 'int',
            'min' -> 1,
            'suggest' -> ['6000']
        }
    },
    'scope' -> 'global'
};

global_lifetime = 6000;

set_lifetime(lifetime) -> (
    global_lifetime = lifetime;
    entity_load_handler('item', _(e, new) -> modify(e, 'nbt_merge', nbt(str('{Age:%d}', (6000-global_lifetime)));));
    print(player(), format(
        'wi  Set maximum item lifetime to ' + lifetime + ' gameticks.'
    ););
);

reset_lifetime() -> (
    entity_load_handler('item', null);
    print(player(), format(
        'wi  Reset maximum item lifetime to 6000 gameticks.'
    ););
);
