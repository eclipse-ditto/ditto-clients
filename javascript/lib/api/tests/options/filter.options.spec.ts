/*!
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import { And, Eq, Exists, Ge, Gt, In, Le, Like, Lt, Ne, Not, Or } from '../../src/options/filter.options';

describe('Filters', () => {
  const prop = 'aProperty';
  const value = 7;
  it('builds an eq', () => {
    const eq = Eq(prop, value);
    expect(eq.toString()).toEqual(`eq(${prop},"${value}")`);
  });
  it('builds an ne', () => {
    const ne = Ne(prop, value);
    expect(ne.toString()).toEqual(`ne(${prop},"${value}")`);
  });
  it('builds a gt', () => {
    const gt = Gt(prop, value);
    expect(gt.toString()).toEqual(`gt(${prop},"${value}")`);
  });
  it('builds a ge', () => {
    const ge = Ge(prop, value);
    expect(ge.toString()).toEqual(`ge(${prop},"${value}")`);
  });
  it('builds a lt', () => {
    const lt = Lt(prop, value);
    expect(lt.toString()).toEqual(`lt(${prop},"${value}")`);
  });
  it('builds a le', () => {
    const le = Le(prop, value);
    expect(le.toString()).toEqual(`le(${prop},"${value}")`);
  });
  it('builds an in', () => {
    const inT = In(prop, value, 'anotherOne');
    expect(inT.toString()).toEqual(`in(${prop},"${value}","anotherOne")`);
  });
  it('builds a like', () => {
    const like = Like(prop, '*test*');
    expect(like.toString()).toEqual(`like(${prop},"*test*")`);
  });
  it('builds an exists', () => {
    const exists = Exists(prop);
    expect(exists.toString()).toEqual(`exists(${prop})`);
  });

  it('builds an and', () => {
    const and = And(Lt(prop, 7), Gt(prop, 5));
    expect(and.toString()).toEqual(`and(lt(${prop},"7"),gt(${prop},"5"))`);
  });
  it('builds an or', () => {
    const or = Or(Lt(prop, 7), Gt(prop, 5));
    expect(or.toString()).toEqual(`or(lt(${prop},"7"),gt(${prop},"5"))`);
  });
  it('builds a not', () => {
    const not = Not(Lt(prop, 7));
    expect(not.toString()).toEqual(`not(lt(${prop},"7"))`);
  });

  it('builds big filters', () => {
    const filter = And(
      Or(
        Ne('Prop1', 9),
        Gt('Prop2', 93)
      ),
      Or(
        Like('Prop3', '*exp'),
        And(
          In('Prop4', 'Option1', 'Option2'),
          Le('Prop4', 'Option3')
        )
      ),
      Not(
        Eq('Prop5', 'Option4')
      )
    );
    expect(filter.toString())
    // tslint:disable-next-line:max-line-length
      .toEqual('and(or(ne(Prop1,"9"),gt(Prop2,"93")),or(like(Prop3,"*exp"),and(in(Prop4,"Option1","Option2"),le(Prop4,"Option3"))),not(eq(Prop5,"Option4")))');
  });
});
